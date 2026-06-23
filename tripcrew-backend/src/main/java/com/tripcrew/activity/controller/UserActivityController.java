package com.tripcrew.activity.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.activity.model.dto.UserActivityResponse;
import com.tripcrew.activity.service.UserActivityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class UserActivityController {
    private final UserActivityService userActivityService;

    @GetMapping("/recent")
    public List<UserActivityResponse> listRecent(@AuthenticationPrincipal Long userId) {
        return userActivityService.listRecent(userId);
    }
}
