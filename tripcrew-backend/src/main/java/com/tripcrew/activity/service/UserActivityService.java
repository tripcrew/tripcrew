package com.tripcrew.activity.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tripcrew.activity.model.dto.UserActivity;
import com.tripcrew.activity.model.dto.UserActivityResponse;
import com.tripcrew.activity.model.mapper.UserActivityMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserActivityService {
    private static final int RECENT_ACTIVITY_LIMIT = 5;

    private final UserActivityMapper userActivityMapper;

    public void record(Long userId, String activityType, Long tripPlanId,
                       String tripPlanTitle, String placeName, Integer visitDay) {
        userActivityMapper.insert(UserActivity.builder()
                .userId(userId).activityType(activityType).tripPlanId(tripPlanId)
                .tripPlanTitle(tripPlanTitle).placeName(placeName).visitDay(visitDay).build());
    }

    public List<UserActivityResponse> listRecent(Long userId) {
        return userActivityMapper.findRecentByUserId(userId, RECENT_ACTIVITY_LIMIT).stream()
                .map(UserActivityResponse::from)
                .toList();
    }
}
