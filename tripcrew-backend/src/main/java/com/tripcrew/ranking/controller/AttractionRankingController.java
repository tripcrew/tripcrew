package com.tripcrew.ranking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.ranking.model.dto.AttractionRankingResponse;
import com.tripcrew.ranking.service.AttractionRankingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class AttractionRankingController {

    private final AttractionRankingService attractionRankingService;

    @GetMapping("/attractions")
    public List<AttractionRankingResponse> getAttractionRankings() {
        return attractionRankingService.getTopFive();
    }
}
