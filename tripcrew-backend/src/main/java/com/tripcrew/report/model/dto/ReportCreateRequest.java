package com.tripcrew.report.model.dto;

import com.tripcrew.report.model.ReportReason;
import com.tripcrew.report.model.ReportTargetType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 신고 생성 요청. 신고자는 인증 주체(JWT)로 정하므로 본문에 받지 않는다.
 * 대상은 폴리모픽(targetType + targetId)이며 존재 검증은 서비스에서 한다.
 */
public record ReportCreateRequest(

        @NotNull
        ReportTargetType targetType,

        @NotNull
        Long targetId,

        @NotNull
        ReportReason reason,

        @Size(max = 500)
        String detail
) {
}
