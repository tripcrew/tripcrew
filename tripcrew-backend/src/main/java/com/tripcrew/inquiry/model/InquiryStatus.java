package com.tripcrew.inquiry.model;

/**
 * 1:1 문의 처리 상태. DB에는 VARCHAR로 이름(name)이 그대로 저장되며 DB DEFAULT 는 'OPEN'.
 */
public enum InquiryStatus {
    /** 접수됨(미답변). 관리자 답변 대기. */
    OPEN,
    /** 관리자 답변 완료. */
    ANSWERED
}
