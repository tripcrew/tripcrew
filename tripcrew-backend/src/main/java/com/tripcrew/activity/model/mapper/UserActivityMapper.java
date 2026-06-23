package com.tripcrew.activity.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.activity.model.dto.UserActivity;

@Mapper
public interface UserActivityMapper {
    int insert(UserActivity activity);

    List<UserActivity> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
