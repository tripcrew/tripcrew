package com.tripcrew.inquiry.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.inquiry.model.dto.Inquiry;
import com.tripcrew.inquiry.model.dto.InquiryCreateRequest;
import com.tripcrew.inquiry.model.dto.InquiryResponse;
import com.tripcrew.inquiry.model.mapper.InquiryMapper;

import lombok.RequiredArgsConstructor;

/**
 * 로드맵 ③ 1:1 문의(사용자). 작성과 내 문의 목록 조회만 담당한다.
 * 작성은 인증 필요(SecurityConfig anyRequest authenticated 로 커버), 조회는 본인 스코프.
 */
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryMapper inquiryMapper;

    /** 문의 작성. 작성자는 인증 주체. status 는 DB DEFAULT 로 OPEN. */
    @Transactional
    public void create(Long userId, InquiryCreateRequest request) {
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .build();
        inquiryMapper.insert(inquiry);
    }

    /** 내 문의 목록(최신순, 답변 포함). 본인 것만 조회된다. */
    @Transactional(readOnly = true)
    public List<InquiryResponse> myList(Long userId) {
        return inquiryMapper.findByUser(userId).stream()
                .map(InquiryResponse::from)
                .toList();
    }
}
