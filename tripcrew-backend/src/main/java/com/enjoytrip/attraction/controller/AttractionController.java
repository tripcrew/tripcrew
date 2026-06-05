package com.enjoytrip.attraction.controller;

import com.enjoytrip.attraction.model.dto.AttractionDto;
import com.enjoytrip.attraction.model.dto.GugunDto;
import com.enjoytrip.attraction.model.dto.SidoDto;
import com.enjoytrip.attraction.service.AttractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/attractions", "/attraction"})
public class AttractionController {

    private final AttractionService attractionService;

    public AttractionController(AttractionService attractionService) {
        this.attractionService = attractionService;
    }

    @GetMapping("/sidos")
    public ResponseEntity<List<SidoDto>> getSidos() {
        return ResponseEntity.ok(attractionService.getSidos());
    }

    @GetMapping("/guguns")
    public ResponseEntity<List<GugunDto>> getGuguns(@RequestParam int sidoCode) {
        return ResponseEntity.ok(attractionService.getGuguns(sidoCode));
    }

    @GetMapping
    public ResponseEntity<List<AttractionDto>> getAttractions(
            @RequestParam(required = false, defaultValue = "0") int sidoCode,
            @RequestParam(required = false, defaultValue = "0") int gugunCode,
            @RequestParam(required = false, defaultValue = "0") int contentTypeId) {
        return ResponseEntity.ok(attractionService.getAttractions(sidoCode, gugunCode, contentTypeId));
    }

    @GetMapping(params = "action=sidos")
    public ResponseEntity<List<SidoDto>> getSidosLegacy() {
        return getSidos();
    }

    @GetMapping(params = "action=guguns")
    public ResponseEntity<List<GugunDto>> getGugunsLegacy(@RequestParam int sidoCode) {
        return getGuguns(sidoCode);
    }

    @GetMapping(params = "action=list")
    public ResponseEntity<AttractionListResponse> getAttractionsLegacy(
            @RequestParam(required = false, defaultValue = "0") int areaCode,
            @RequestParam(required = false, defaultValue = "0") int sigunguCode,
            @RequestParam(required = false, defaultValue = "0") int contentTypeId) {
        return ResponseEntity.ok(new AttractionListResponse(
                attractionService.getAttractions(areaCode, sigunguCode, contentTypeId)));
    }

    public record AttractionListResponse(List<AttractionDto> items) {
    }
}
