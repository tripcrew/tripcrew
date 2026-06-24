package com.tripcrew.inquiry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.inquiry.model.InquiryStatus;
import com.tripcrew.inquiry.model.dto.AdminInquiryResponse;
import com.tripcrew.inquiry.model.dto.Inquiry;
import com.tripcrew.inquiry.model.mapper.InquiryMapper;
import com.tripcrew.notification.model.NotificationType;
import com.tripcrew.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * 로드맵 ③ 1:1 문의(관리자). 진입은 ROLE_ADMIN 으로 SecurityConfig(/api/admin/**)에서 막혀 있다.
 * 미답변(OPEN) 문의를 검토해 답변하면 상태가 ANSWERED 로 바뀐다.
 *
 * <p><b>재답변 정책</b>: 이미 ANSWERED 인 문의도 답변을 수정할 수 있다(오타/보강).
 * 단 작성자 알림은 <b>최초 답변(OPEN→ANSWERED) 때만</b> 보낸다(수정 시 알림 스팸 방지).
 */
@Service
@RequiredArgsConstructor
public class AdminInquiryService {

    private final InquiryMapper inquiryMapper;
    private final NotificationService notificationService;

    /** 관리자 문의 목록. status 가 null 이면 전체, 아니면 해당 상태만(보통 OPEN). */
    @Transactional(readOnly = true)
    public List<AdminInquiryResponse> list(InquiryStatus status) {
        return inquiryMapper.findAllForAdmin(status).stream()
                .map(AdminInquiryResponse::from)
                .toList();
    }

    /**
     * 답변 등록/수정. status=ANSWERED 로 전환하고 answer/answered_by/answered_at 을 채운다.
     * 최초 답변(직전 OPEN)일 때만 작성자에게 INQUIRY_ANSWERED 알림을 보낸다(같은 트랜잭션 — 원자적).
     *
     * @param adminId 답변한 관리자 id(인증 주체)
     */
    @Transactional
    public void answer(Long inquiryId, Long adminId, String answer) {
        Inquiry inquiry = inquiryMapper.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."));

        boolean firstAnswer = inquiry.getStatus() == InquiryStatus.OPEN;
        inquiryMapper.answer(inquiryId, answer, adminId, LocalDateTime.now());

        if (firstAnswer) {
            notificationService.notify(inquiry.getUserId(), NotificationType.INQUIRY_ANSWERED,
                    inquiryId, answeredMessage(inquiry));
        }
    }

    /** 답변 완료 알림 문구. 어떤 문의인지 제목으로 맥락을 준다. */
    private String answeredMessage(Inquiry inquiry) {
        return String.format("문의하신 '%s'에 대한 답변이 등록되었습니다.", inquiry.getTitle());
    }
}
