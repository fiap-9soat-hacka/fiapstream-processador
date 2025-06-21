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

import com.fiap.dto.ResponseData;
import com.fiap.services.ProcessadorService;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
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
    @RunOnVirtualThread
    public Response uploadVideos(String message) throws Throwable, Exception {
        Log.info("Received request: " + message);
        processadorService.processarVideo(message);
        // throw new Exception("Erro ao processar o vídeo");
        return Response.ok("Processamento finalizado com sucesso!").build();
    }

    @Incoming("processador-requests.dlq")
    public Response processarDLX1(String message) throws InterruptedException {
        Log.info("===================2");
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response webhook(ResponseData responseData) {
        Log.info("Received response data: " + responseData);
        return Response.ok("Webhook Enviado com sucesso").build();
    }
}
