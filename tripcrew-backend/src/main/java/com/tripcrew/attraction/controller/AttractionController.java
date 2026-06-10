package com.tripcrew.attraction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.attraction.model.dto.AttractionDetailResponse;
import com.tripcrew.attraction.model.dto.AttractionPageResponse;
import com.tripcrew.attraction.model.dto.AttractionSearchRequest;
import com.tripcrew.attraction.service.AttractionService;

import lombok.RequiredArgsConstructor;

/**
 * F02 관광지 공개 조회 API.
 * 검색어/필터가 바뀔 때마다 같은 목록 API에 query parameter 를 조합해 요청한다.
 */
@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @GetMapping
    public AttractionPageResponse search(@ModelAttribute AttractionSearchRequest request) {
        return attractionService.search(request);
    }

    @GetMapping("/{no}")
    public AttractionDetailResponse get(@PathVariable Integer no) {
        return attractionService.get(no);
    }
}
