package com.tripcrew.region.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tripcrew.region.model.dto.GugunResponse;
import com.tripcrew.region.model.dto.SidoResponse;
import com.tripcrew.region.model.mapper.RegionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionMapper regionMapper;

    public List<SidoResponse> listSidos() {
        return regionMapper.findSidos();
    }

    public List<GugunResponse> listGuguns(Integer sidoCode) {
        return regionMapper.findGugunsBySidoCode(sidoCode);
    }
}
