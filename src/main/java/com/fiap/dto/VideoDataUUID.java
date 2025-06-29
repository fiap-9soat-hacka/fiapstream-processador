package com.fiap.dto;

public record VideoDataUUID(
        String filename,
        String mimeType,
        String uuid,
        String webhookUrl) {
}
