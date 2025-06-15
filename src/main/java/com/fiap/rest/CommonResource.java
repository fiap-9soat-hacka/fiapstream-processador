package com.fiap.rest;

import java.io.File;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fiap.dto.VideoDataUUID;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ApplicationScoped
public class CommonResource {

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    public GetObjectRequest buildGetRequest(String objectKey) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

    public PutObjectRequest buildPutRequest(String key, String mimeType) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(mimeType)
                .build();
    }
}