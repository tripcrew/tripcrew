package com.tripcrew.tripplan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.tripplan.model.dto.InviteResponse;
import com.tripcrew.tripplan.service.TripMemberService;

import lombok.RequiredArgsConstructor;

/**
 * F06 공동편집 P4 — 피초대자 본인의 초대 처리. 인증 필요(SecurityConfig anyRequest authenticated 로 커버).
 *   GET    /api/me/invites               내가 받은(수락 대기) 초대 목록
 *   POST   /api/me/invites/{planId}/accept  수락(PENDING → ACCEPTED)
 *   DELETE /api/me/invites/{planId}      거절(PENDING 행 삭제)
 *
 * 대상은 항상 인증 주체 본인이므로 planId 만 받는다(타인 초대 처리 불가).
 */
@RestController
@RequestMapping("/api/me/invites")
@RequiredArgsConstructor
public class MyInviteController {

    private final TripMemberService tripMemberService;

    @GetMapping
    public List<InviteResponse> list(@AuthenticationPrincipal Long userId) {
        return tripMemberService.listMyInvites(userId);
    }

    @PostMapping("/{planId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@AuthenticationPrincipal Long userId, @PathVariable Long planId) {
        tripMemberService.acceptInvite(planId, userId);
    }

    @DeleteMapping("/{planId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@AuthenticationPrincipal Long userId, @PathVariable Long planId) {
        tripMemberService.rejectInvite(planId, userId);
    }
}
