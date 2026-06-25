package com.tripcrew.admin.model.dto;

import java.time.Instant;
import java.util.List;

public record ExternalApiHealthResponse(
        Instant checkedAt,
        List<ExternalApiHealthItem> services
) {
}
