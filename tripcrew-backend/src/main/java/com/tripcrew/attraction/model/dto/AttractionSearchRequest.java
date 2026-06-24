package com.tripcrew.attraction.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionSearchRequest {

    private static final int DEFAULT_SIZE = 6;
    private static final int MAX_SIZE = 60;
    private static final int MIN_KEYWORD_LENGTH = 2;

    private String keyword;
    private Integer sidoCode;
    private Integer gugunCode;
    private List<Integer> contentTypeIds;
    private Double minRating;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = DEFAULT_SIZE;

    public void normalize() {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.length() < MIN_KEYWORD_LENGTH) {
                keyword = null;
            }
        }
        if (minRating != null && (minRating < 1 || minRating > 5)) {
            minRating = null;
        }
    }

    public int getOffset() {
        return (getSafePage() - 1) * getLimit();
    }

    public int getLimit() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public int getPage() {
        return getSafePage();
    }

    private int getSafePage() {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }
}
