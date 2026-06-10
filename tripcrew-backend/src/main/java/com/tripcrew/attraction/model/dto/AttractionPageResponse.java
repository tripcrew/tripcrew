package com.tripcrew.attraction.model.dto;

import java.util.List;

public record AttractionPageResponse(
        List<AttractionSummaryResponse> items,
        int page,
        int size,
        long totalCount,
        int totalPages
) {
    public static AttractionPageResponse of(
            List<AttractionSummaryResponse> items,
            int page,
            int size,
            long totalCount
    ) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalCount / size);
        return new AttractionPageResponse(items, page, size, totalCount, totalPages);
    }
}
