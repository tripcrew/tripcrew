package com.tripcrew.attraction.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tripcrew.attraction.model.dto.AttractionDetailResponse;
import com.tripcrew.attraction.model.dto.AttractionPageResponse;
import com.tripcrew.attraction.model.dto.AttractionSearchRequest;
import com.tripcrew.attraction.model.dto.AttractionSummaryResponse;
import com.tripcrew.attraction.model.mapper.AttractionMapper;
import com.tripcrew.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionMapper attractionMapper;

    public AttractionPageResponse search(AttractionSearchRequest request) {
        request.normalize();

        List<AttractionSummaryResponse> items = attractionMapper.search(request);
        long totalCount = attractionMapper.count(request);

        return AttractionPageResponse.of(items, request.getPage(), request.getLimit(), totalCount);
    }

    public AttractionDetailResponse get(Integer no) {
        return attractionMapper.findByNo(no)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "관광지를 찾을 수 없습니다."));
    }
}
