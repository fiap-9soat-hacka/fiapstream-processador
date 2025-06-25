package com.fiap.services;

import java.io.IOException;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.dto.MessageResponseData;
import com.fiap.dto.VideoDataUUID;
import com.fiap.enums.EstadoProcessamento;
import com.fiap.utils.StringUtils;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class DeadLetterQueueService {

    @Inject
    ProcessadorService processadorService;

    @Inject
    StringUtils stringUtils;

    @Channel("processador-responses")
    Emitter<String> responseEmitter;

    @Retry(maxRetries = 3)
    @Fallback(fallbackMethod = "metodoFallback", applyOn = Exception.class)
    public void retentativaProcessamento(String message) throws IOException, Exception {
        Log.info("Trying to reprocess the DLQ message");
        processadorService.processarVideo(message);
        // throw new Exception("Erro ao processar o vídeo");
    }

    public void metodoFallback(String message, Exception cause) {
        Log.info("Fallback method triggered for error: " + cause);
        VideoDataUUID videoData = stringUtils.convert(message, VideoDataUUID.class);

        MessageResponseData responseData = new MessageResponseData(
                videoData.filename(),
                videoData.uuid(),
                EstadoProcessamento.ERRO);
        sendResponse(responseData);
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
        responseEmitter.send(responseJson);
    }
}
