package com.fiap.services;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.dto.MessageResponseData;
import com.fiap.dto.VideoDataUUID;
import com.fiap.integration.s3.CommonResource;
import com.fiap.utils.StringUtils;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@QuarkusTest
public class ProcessadorServiceTest {

    @Inject
    ProcessadorService processadorService;

    @InjectMock
    S3Client s3Client;

    @InjectMock
    CommonResource commonResource;

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Inject
    StringUtils stringUtils;

    @Test
    void testaSucessoProcessarVideo() throws IOException {
        InMemorySink<String> responsesOut = connector.sink("processador-responses");

        VideoDataUUID videoData = new VideoDataUUID(
                "teste", "video/mp4",
                "teste", "http://teste.com");
        File file = new File("src/test/resources/testevideo.mp4");
        byte[] objectBytes = Files.readAllBytes(file.toPath());

        GetObjectRequest mockGetObjectRequest = GetObjectRequest.builder().key(videoData.uuid()).build();
        when(commonResource.buildGetRequest(videoData.uuid())).thenReturn(mockGetObjectRequest);

        ResponseBytes<GetObjectResponse> mockResponseBytes = Mockito.mock(ResponseBytes.class);
        when(mockResponseBytes.asByteArray()).thenReturn(objectBytes);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(mockResponseBytes);

        PutObjectResponse putResponse = PutObjectResponse.builder().size(1L).versionId("1").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putResponse);

        ObjectMapper mapper = new ObjectMapper();
        assertThrows(WebApplicationException.class,
                () -> processadorService.processarVideo(mapper.writeValueAsString(videoData)));

        MessageResponseData response = stringUtils.convert(responsesOut.received().get(1).getPayload(),
                MessageResponseData.class);
        assertEquals(2, responsesOut.received().size());
        assertEquals("teste", response.getFilename());
        assertEquals("teste", response.getKey());
    }
}
