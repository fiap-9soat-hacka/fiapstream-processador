package com.fiap.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import jakarta.ws.rs.core.Response;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.xml.sax.SAXException;

import com.fiap.services.ProcessadorService;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/processador")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.TEXT_PLAIN)
public class ProcessadorResource {

    @Inject
    ProcessadorService processadorService;

    @Incoming("processador-requests")
    public Response teste(String message) throws Throwable, Exception {
        // Log.info("Received request: " + message);
        // processadorService.processarVideo(message);
        throw new Exception("Erro ao processar o vídeo");
        // return Response.ok("Processamento finalizado com sucesso!").build();
    }

    // public static class VideoUploadForm {
    // @RestForm("files")
    // @PartType(MediaType.APPLICATION_OCTET_STREAM)
    // public List<File> files;
    // }

    // @GET
    // public Response testDownload() throws IOException, Exception {
    // File generatedFile = this.processadorService.processarVideo();

    // return Response
    // .ok(Files.readAllBytes(generatedFile.toPath()))
    // .header("Content-Disposition", String.format("attachment; filename=\"%s\"",
    // generatedFile.getName()))
    // .build();
    // }

    // @POST
    // @Consumes(MediaType.MULTIPART_FORM_DATA)
    // @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // public Response uploadVideos(VideoUploadForm form) throws IOException,
    // SAXException, TikaException, Exception {
    // File generatedFile = this.processadorService.processarVideo();

    // return Response
    // .ok(Files.readAllBytes(generatedFile.toPath()))
    // .header("Content-Disposition", "attachment; filename=\"example.zip\"")
    // .build();
    // }

    @Incoming("processador-requests.dlq")
    public Response processarDLX1(String message) throws InterruptedException {
        return Response.ok().build();
    }
}
