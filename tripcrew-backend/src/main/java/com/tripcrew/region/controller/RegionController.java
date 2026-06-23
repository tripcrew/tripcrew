package com.tripcrew.region.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.region.model.dto.GugunResponse;
import com.tripcrew.region.model.dto.SidoResponse;
import com.tripcrew.region.service.RegionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/sidos")
    public List<SidoResponse> listSidos() {
        return regionService.listSidos();
    }

    @GetMapping("/sidos/{sidoCode}/guguns")
    public List<GugunResponse> listGuguns(@PathVariable Integer sidoCode) {
        return regionService.listGuguns(sidoCode);
    }
}
