package com.tripcrew.attraction.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.tripcrew.attraction.model.dto.AttractionDetailResponse;
import com.tripcrew.attraction.model.dto.AttractionSearchRequest;
import com.tripcrew.attraction.model.dto.AttractionSummaryResponse;

@Mapper
public interface AttractionMapper {

    List<AttractionSummaryResponse> search(AttractionSearchRequest request);

    long count(AttractionSearchRequest request);

    Optional<AttractionDetailResponse> findByNo(Integer no);
}
