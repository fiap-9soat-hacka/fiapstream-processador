package com.fiap.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StringUtils {

    @Inject
    ObjectMapper objectMapper;

    public <T> T convert(String json, Class<T> targetType) {
        try {
            return objectMapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert", e);
        }
    }
}
