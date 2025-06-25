package com.fiap.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import org.apache.james.mime4j.util.MimeUtil;
import org.apache.tika.Tika;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.dto.MessageResponseData;
import com.fiap.dto.VideoDataUUID;
import com.fiap.enums.EstadoProcessamento;
import com.fiap.integration.s3.CommonResource;
import com.fiap.utils.MimeUtils;
import com.fiap.utils.StringUtils;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class ProcessadorService {

    @Inject
    public ProcessadorService() {
    }

    @Inject
    S3Client s3Client;

    @Inject
    CommonResource commonResource;

    @Inject
    StringUtils stringUtils;

    @Channel("processador-responses")
    Emitter<String> responseEmitter;

    // public static void main(String[] args) {
    // try {
    // new ProcessadorService().processarVideo();
    // } catch (Exception | IOException e) {
    // throw new RuntimeException(e);
    // }
    // }

    /**
     * TODO: Fazer processamento com arquivo recebido do resource
     * TODO: Mover esse código para sistema de filas
     *
     * @return
     * @throws IOException
     * @throws Exception
     */

    public void processarVideo(String request) throws IOException {
        // Pegando arquivo salvo no s3
        VideoDataUUID videoData = stringUtils.convert(request, VideoDataUUID.class);
        byte[] objectBytes = s3Client.getObjectAsBytes(commonResource.buildGetRequest(videoData.uuid()))
                .asByteArray();

        Tika tika = new Tika();
        String mimeType = tika.detect(objectBytes);
        mimeType = MimeUtils.getExtensionFromMimeType(mimeType);
        Path tempFile = Files.createTempFile("video-", mimeType);
        Files.write(tempFile, objectBytes, StandardOpenOption.WRITE);

        // Vídeo em processamento
        MessageResponseData responseData = new MessageResponseData(
                videoData.filename(),
                videoData.uuid(),
                EstadoProcessamento.PROCESSANDO);
        sendResponse(responseData);

        Path tmpdir = processarVideoEmImagens(tempFile);

        Log.info("Processamento finalizado para: " + tempFile.getFileName());

        // Zipando arquivos e enviando para o S3
        File zipData = criarArquivoZipado(tmpdir);
        PutObjectResponse putResponse = s3Client.putObject(
                commonResource.buildPutRequest(videoData.uuid() + ".zip", "application/zip"),
                RequestBody.fromFile(zipData));
        if (putResponse == null) {
            throw new WebApplicationException("Failed to upload file to S3");
        }

        responseData.setEstado(EstadoProcessamento.CONCLUIDO);
        sendResponse(responseData);
        Log.info("Zip salvo no S3");
    }

    private Path processarVideoEmImagens(Path tempFile) throws IOException {
        // instalar ffmpeg e ffprobe:
        // sudo apt install ffmpeg
        FFmpeg ffmpeg = new FFmpeg();
        FFprobe ffprobe = new FFprobe();

        FFmpegProbeResult in = ffprobe.probe(tempFile.toString());
        Path tmpdir = Files
                .createTempDirectory("fiapstream-process-")
                .toAbsolutePath();
        String tmpdirPath = tmpdir.toString();

        Log.info("Processando video " + tempFile.getFileName());
        Log.info("Resultado do processamento temporariamente armazenado em " + tmpdir);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(tempFile.toString())
                .overrideOutputFiles(true)
                .addOutput(tmpdirPath + File.separator + "out-%03d.jpg")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        FFmpegJob job = executor.createJob(builder, new ProgressListener() {

            // Using the FFmpegProbeResult determine the duration of the input
            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            @Override
            public void progress(Progress progress) {
                double percentage = progress.out_time_ns / duration_ns;

                // Print out interesting information about the progress
                Log.info(String.format(
                        "[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
                        percentage * 100,
                        progress.status,
                        progress.frame,
                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                        progress.fps.doubleValue(),
                        progress.speed));
            }
        });

        job.run();

        return tmpdir;
    }

    private void sendResponse(MessageResponseData responseData) {
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(responseData);
        } catch (IOException e) {
            Log.error("Error converting response data to JSON", e);
            throw new WebApplicationException("Failed to convert response data to JSON", e);
        }
        Log.info("Enviando resposta: " + responseJson);
        responseEmitter.send(responseJson); // Return the JSON string for further processing
    }

    private File criarArquivoZipado(Path jobResultDirectory) throws IOException {
        File targetFile = jobResultDirectory.toFile();
        File zipFile = Files.createTempFile("processed-", ".zip").toFile(); // Create a temporary file for the zip

        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)) {

            File[] targetFiles = targetFile.listFiles();
            assert targetFiles != null;

            for (File f : targetFiles) {
                ZipEntry zipEntry = new ZipEntry(f.getName());
                zos.putNextEntry(zipEntry);
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }

        } catch (IOException e) {
            throw new WebApplicationException(e);
        }

        return zipFile; // Return the created zip file
    }
}
