package com.fiap.resource;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fiap.dto.ResponseData;
import com.fiap.services.DeadLetterQueueService;
import com.fiap.services.ProcessadorService;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/processador")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.TEXT_PLAIN)
public class ProcessadorResource {

    @Inject
    ProcessadorService processadorService;

    @Inject
    DeadLetterQueueService deadLetterQueueService;

    @Incoming("processador-requests")
    @RunOnVirtualThread
    public Response uploadVideos(String message) throws Throwable, Exception {
        Log.info("Received request: " + message);
        // throw new Exception("Erro ao processar o vídeo");
        processadorService.processarVideo(message);
        return Response.ok("Processamento finalizado com sucesso!").build();
    }

    @Incoming("processador-requests.dlq")
    public Response processarDLX1(String message) throws java.lang.Exception {
        Log.info("Retrying message from DLQ: " + message);
        deadLetterQueueService.retentativaProcessamento(message);
        return Response.ok().build();
    }
}
