package com.fiap.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.dto.MessageResponseData;
import com.fiap.enums.EstadoProcessamento;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@QuarkusTest
public class DeadLetterQueueServiceTest {

    @InjectSpy
    DeadLetterQueueService deadLetterQueueService;

    @InjectMock
    ProcessadorService processadorService;

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Test
    void testaSucessoReprocessamento() {
        MessageResponseData data = new MessageResponseData();
        data.setEstado(EstadoProcessamento.PENDENTE);
        data.setKey("teste");
        data.setFilename("teste");

        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> deadLetterQueueService.retentativaProcessamento(mapper.writeValueAsString(data)));
    }

    @Test
    void testaFalhaReprocessamento() throws Exception {
        MessageResponseData data = new MessageResponseData();
        data.setEstado(EstadoProcessamento.PENDENTE);
        data.setKey("teste");
        data.setFilename("teste");

        doThrow(WebApplicationException.class).when(processadorService).processarVideo(any());

        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> deadLetterQueueService.retentativaProcessamento(mapper.writeValueAsString(data)));
        verify(processadorService, times(4)).processarVideo(any());
        verify(deadLetterQueueService, times(1)).metodoFallback(any(), any());
    }
}
