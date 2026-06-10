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
public class AttractionDetailResponse {

    private Integer no;
    private Integer contentId;
    private String title;
    private Integer contentTypeId;
    private String contentType;
    private Integer areaCode;
    private Integer siGunGuCode;
    private String sido;
    private String gugun;
    private String firstImage1;
    private String firstImage2;
    private Integer mapLevel;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String tel;
    private String addr1;
    private String addr2;
    private String homepage;
    private String overview;
}
