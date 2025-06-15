package com.fiap.dto;

import java.io.File;

public record VideoDataUUID(
        File video,
        String filename,
        String mimeType,
        String uuid) {
}
