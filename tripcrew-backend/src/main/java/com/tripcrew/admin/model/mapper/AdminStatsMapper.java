package com.tripcrew.admin.model.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.admin.model.dto.DailyCount;
import com.tripcrew.admin.model.dto.LabelCount;

/**
 * 관리자 대시보드 차트용 집계 매퍼. users/reviews/reports/chat_messages 의 카운트만 읽는 self-contained 매퍼라
 * 도메인 매퍼(공유 코드)를 건드리지 않는다. 전부 GROUP BY 카운트 — 마이그레이션 불필요.
 */
@Mapper
public interface AdminStatsMapper {

    /** [start, endExclusive) 구간의 일자별 가입 수(빈 날짜는 결과에 없음 → 서비스에서 0 채움). */
    List<DailyCount> signupsBetween(@Param("start") LocalDateTime start,
                                    @Param("endExclusive") LocalDateTime endExclusive);

    /** since 이후 일자별 후기 작성 수. */
    List<DailyCount> reviewsByDay(@Param("since") LocalDateTime since);

    /** since 이후 일자별 신고 접수 수. */
    List<DailyCount> reportsByDay(@Param("since") LocalDateTime since);

    /** 누적 챗봇 요청 수(role='USER' 행 = 사용자 턴 수, 응답 행 제외). */
    long countChatbotRequests();

    /** since 이후 일자별 챗봇 요청 수(role='USER'). */
    List<DailyCount> chatbotByDay(@Param("since") LocalDateTime since);

    /** 회원 역할 분포(USER/ADMIN/SUPER_ADMIN). */
    List<LabelCount> roleDistribution();

    /** 회원 상태 분포(ACTIVE/BANNED/WITHDRAWN). */
    List<LabelCount> statusDistribution();
}
