package com.fiap.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.Test;

import com.fiap.RabbitMqQuarkusTestResource;
import com.fiap.services.DeadLetterQueueService;
import com.fiap.services.ProcessadorService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(RabbitMqQuarkusTestResource.class)
class ProcessadorResourceTest {

    @InjectMock
    ProcessadorService processadorService;

    @InjectMock
    DeadLetterQueueService deadLetterQueueService;

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Test
    void testaSucessoUploadVideos() {
        InMemorySource<String> ordersIn = connector.source("processador-requests");

        ordersIn.send("teste");
    }

}
