package com.fiap.dto;

import java.io.File;

public record VideoDataUUID(
        String filename,
        String mimeType,
        String uuid,
        String webhookUrl) {
}
