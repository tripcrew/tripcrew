package com.enjoytrip.attraction.service;

import com.enjoytrip.attraction.model.dto.AttractionDto;
import com.enjoytrip.attraction.model.dto.GugunDto;
import com.enjoytrip.attraction.model.dto.SidoDto;
import com.enjoytrip.attraction.model.mapper.AttractionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttractionService {

    private final AttractionMapper attractionMapper;

    @Autowired
    public AttractionService(AttractionMapper attractionMapper) {
        this.attractionMapper = attractionMapper;
    }

    public List<SidoDto> getSidos() {
        return attractionMapper.getSidos();
    }

    public List<GugunDto> getGuguns(int sidoCode) {
        return attractionMapper.getGuguns(sidoCode);
    }

    public List<AttractionDto> getAttractions(int sidoCode, int gugunCode, int contentTypeId) {
        Map<String, Integer> params = new HashMap<>();
        params.put("sidoCode", sidoCode);
        params.put("gugunCode", gugunCode);
        params.put("contentTypeId", contentTypeId);
        return attractionMapper.getAttractions(params);
    }

}
