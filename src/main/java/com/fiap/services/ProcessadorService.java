package com.fiap.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.bytedeco.javacv.FrameGrabber.Exception;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class ProcessadorService {

    @Inject
    public ProcessadorService() {
    }

    public static void main(String[] args) {
        try {
            new ProcessadorService().processarVideo();
        } catch (Exception | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO: Fazer processamento com arquivo recebido do resource
     * TODO: Mover esse código para sistema de filas
     *
     * @return
     * @throws IOException
     * @throws Exception
     */
    public File processarVideo() throws IOException, Exception {
        final String fileNameWithExtension = "testevideo.mp4";

        // instalar ffmpeg e ffprobe:
        // sudo apt install ffmpeg
        FFmpeg ffmpeg = new FFmpeg();
        FFprobe ffprobe = new FFprobe();

        FFmpegProbeResult in = ffprobe.probe(fileNameWithExtension);
        Path tmpdir = Files
            .createTempDirectory("fiapstream-process-")
            .toAbsolutePath();
        String tmpdirPath = tmpdir.toString();

        System.out.printf("Processando video %s\n", fileNameWithExtension);
        System.out.printf("Resultado do processamento temporariamente armazenado em %s\n", tmpdir);


        FFmpegBuilder builder = new FFmpegBuilder()
            .setInput(fileNameWithExtension)
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
                System.out.printf(
                    "[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx%n",
                    percentage * 100,
                    progress.status,
                    progress.frame,
                    FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                    progress.fps.doubleValue(),
                    progress.speed
                );
            }
        });

        job.run();

        System.out.printf("Processamento finalizado para: %s", fileNameWithExtension);

        return this.criarArquivoZipado(tmpdir);
    }

    public File criarArquivoZipado(Path jobResultDirectory) throws FileNotFoundException {
        String targetZipFilename = jobResultDirectory.getFileName() + ".zip";
        File targetFile = jobResultDirectory.toFile();

        File targetZipFile = new File("outputs/" + targetZipFilename);
        FileOutputStream fos = new FileOutputStream(targetZipFile);
        try (ZipOutputStream zos = new ZipOutputStream(fos)) {
            File[] targetFiles = targetFile.listFiles();
            assert targetFiles != null;

            for (File f : targetFiles) {
                ZipEntry zipEntry = new ZipEntry(f.getName());
                zos.putNextEntry(zipEntry);
                FileInputStream fis = new FileInputStream(f);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }

        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        return targetZipFile;
    }
}
