package com.enjoytrip.attraction.model.mapper;

import com.enjoytrip.attraction.model.dto.AttractionDto;
import com.enjoytrip.attraction.model.dto.GugunDto;
import com.enjoytrip.attraction.model.dto.SidoDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface AttractionMapper {
    // TODO: [Mapper의 역할] 이 인터페이스의 메서드명은 XML의 id와 매핑됩니다.
    //  sido 테이블의 모든 시도 정보를 조회하는 SQL을 XML에 작성하세요.
    List<SidoDto> getSidos();

    // TODO: [Mapper의 역할] sido_code에 해당하는 구군 정보를 조회하는 SQL을 XML에 작성하세요.
    List<GugunDto> getGuguns(int sidoCode);

    // TODO: [Mapper의 역할] 파라미터(areaCode, sigunguCode, contentTypeId)에 맞는 관광지 정보를 조회하는 SQL을 XML에 작성하세요.
    List<AttractionDto> getAttractions(Map<String, Integer> params);
}
