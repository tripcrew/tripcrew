package com.tripcrew.admin.model.dto;

public record ExternalApiHealthItem(
        String key,
        String name,
        String status,
        String message,
        Long latencyMs
) {
}
