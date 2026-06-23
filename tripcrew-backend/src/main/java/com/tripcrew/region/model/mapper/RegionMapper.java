package com.tripcrew.region.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tripcrew.region.model.dto.GugunResponse;
import com.tripcrew.region.model.dto.SidoResponse;

@Mapper
public interface RegionMapper {

    List<SidoResponse> findSidos();

    List<GugunResponse> findGugunsBySidoCode(Integer sidoCode);
}
