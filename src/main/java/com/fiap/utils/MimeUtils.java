package com.fiap.utils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MimeUtils {
    public static String getExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            case "video/mp4":
                return ".mp4";
            case "video/x-msvideo":
                return ".avi";
            case "video/x-matroska":
                return ".mkv";
            case "video/quicktime":
                return ".mov";
            default:
                return "";
        }
    }
}
