package com.tripcrew.attraction.model.dto;

import java.math.BigDecimal;

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
public class AttractionSummaryResponse {

    private Integer no;
    private String title;
    private String imageUrl;
    private String sido;
    private String gugun;
    private String contentType;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
