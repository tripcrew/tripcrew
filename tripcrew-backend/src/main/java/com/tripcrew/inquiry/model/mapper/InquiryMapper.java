package com.tripcrew.inquiry.model.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.inquiry.model.InquiryStatus;
import com.tripcrew.inquiry.model.dto.AdminInquiryRow;
import com.tripcrew.inquiry.model.dto.Inquiry;

@Mapper
public interface InquiryMapper {

    /** 문의 적재. status·created_at·updated_at 은 DB DEFAULT. 생성된 id 는 dto 에 채워진다. */
    int insert(Inquiry inquiry);

    /** 문의 한 건 조회(답변 처리 시 작성자/상태 확인용). */
    Optional<Inquiry> findById(@Param("id") Long id);

    /** 내 문의 목록(최신순). */
    List<Inquiry> findByUser(@Param("userId") Long userId);

    /** 관리자 문의 목록(작성자 이메일 조인). status 가 null 이면 전체, 아니면 해당 상태만. */
    List<AdminInquiryRow> findAllForAdmin(@Param("status") InquiryStatus status);

    /** 상태별 문의 수(대시보드 집계: 보통 OPEN 미답변 건수). */
    long countByStatus(@Param("status") InquiryStatus status);

    /** 답변 등록/수정. status=ANSWERED 로 전환하고 answer/answered_by/answered_at 을 채운다. */
    int answer(@Param("id") Long id,
               @Param("answer") String answer,
               @Param("answeredBy") Long answeredBy,
               @Param("answeredAt") LocalDateTime answeredAt);
}
