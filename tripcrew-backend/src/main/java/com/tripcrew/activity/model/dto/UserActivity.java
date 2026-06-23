package com.tripcrew.activity.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {
    private Long id;
    private Long userId;
    private String activityType;
    private Long tripPlanId;
    private String tripPlanTitle;
    private String placeName;
    private Integer visitDay;
    private LocalDateTime createdAt;
}
