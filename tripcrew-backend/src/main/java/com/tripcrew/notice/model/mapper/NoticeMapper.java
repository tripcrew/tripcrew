package com.tripcrew.notice.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.notice.model.dto.Notice;

@Mapper
public interface NoticeMapper {

    /** 성공 시 notice.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(Notice notice);

    /** 단건 조회. 작성 관리자 닉네임을 users 조인으로 채운다. */
    Optional<Notice> findById(@Param("id") Long id);

    /** 전체 목록(고정 우선, 최신순). 작성 관리자 닉네임 포함. */
    List<Notice> findAll();

    /** 제목/내용/고정 여부 수정. 영향 행 수 반환(없는 id 면 0). */
    int update(Notice notice);

    /** 삭제. 영향 행 수 반환(없는 id 면 0). */
    int deleteById(@Param("id") Long id);

    /** 조회수 +1(상세 조회 시). 영향 행 수 반환. */
    int incrementViewCount(@Param("id") Long id);
}
