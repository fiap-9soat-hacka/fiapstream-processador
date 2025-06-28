package com.fiap.resource;

import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.Test;

import com.fiap.RabbitMqQuarkusTestResource;
import com.fiap.services.DeadLetterQueueService;
import com.fiap.services.ProcessadorService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(RabbitMqQuarkusTestResource.class)
public class ProcessadorResourceTest {

    @InjectMock
    ProcessadorService processadorService;

    @InjectMock
    DeadLetterQueueService deadLetterQueueService;

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Test
    void testaSucessoUploadVideos() {

    }
}
