package com.tripcrew.tripplan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.coedit.dto.PlaceChangeAction;
import com.tripcrew.coedit.edit.PlaceChangeBroadcaster;
import com.tripcrew.tripplan.model.dto.MemberInviteRequest;
import com.tripcrew.tripplan.model.dto.MemberRoleUpdateRequest;
import com.tripcrew.tripplan.model.dto.TripMemberResponse;
import com.tripcrew.tripplan.service.TripMemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * F06 공동편집 — 여행계획 멤버(협업자) 관리. 인증 필요(SecurityConfig 의 anyRequest().authenticated()).
 * 목록은 모든 멤버, 초대/역할변경/타인제거는 소유자만, 탈퇴는 본인만 가능(서비스에서 권한 판정).
 */
@RestController
@RequestMapping("/api/trip-plans/{planId}/members")
@RequiredArgsConstructor
public class TripMemberController {

    private final TripMemberService tripMemberService;
    private final PlaceChangeBroadcaster placeChangeBroadcaster;

    @GetMapping
    public List<TripMemberResponse> list(@AuthenticationPrincipal Long userId,
                                         @PathVariable Long planId) {
        return tripMemberService.list(planId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripMemberResponse invite(@AuthenticationPrincipal Long userId,
                                     @PathVariable Long planId,
                                     @Valid @RequestBody MemberInviteRequest request) {
        return tripMemberService.invite(planId, userId, request.email(), request.role());
    }

    @PatchMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(@AuthenticationPrincipal Long userId,
                           @PathVariable Long planId,
                           @PathVariable Long targetUserId,
                           @Valid @RequestBody MemberRoleUpdateRequest request) {
        tripMemberService.updateRole(planId, userId, targetUserId, request.role());
    }

    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Long userId,
                       @PathVariable Long planId,
                       @PathVariable Long targetUserId) {
        tripMemberService.remove(planId, userId, targetUserId);
        // 소유자가 타인을 내보낸 경우(본인 탈퇴 제외) 대상에게 실시간으로 알려 편집 화면에서 내보낸다.
        if (!userId.equals(targetUserId)) {
            placeChangeBroadcaster.broadcast(planId, userId, PlaceChangeAction.MEMBER_REMOVED, targetUserId);
        }
    }
}
