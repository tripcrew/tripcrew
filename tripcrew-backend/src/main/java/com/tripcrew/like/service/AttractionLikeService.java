package com.tripcrew.like.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.like.dto.LikeStatusResponse;
import com.tripcrew.like.model.mapper.AttractionLikeMapper;

import lombok.RequiredArgsConstructor;

/**
 * 관광지 좋아요(찜). 한 사용자가 한 관광지에 한 번(유니크), POST=좋아요/DELETE=취소 토글.
 * 좋아요 수는 COUNT 로 집계해 매번 최신값을 함께 돌려준다.
 */
@Service
@RequiredArgsConstructor
public class AttractionLikeService {

    private final AttractionLikeMapper likeMapper;

    /** 좋아요 상태 조회(공개). userId 가 null(비로그인)이면 liked=false. */
    @Transactional(readOnly = true)
    public LikeStatusResponse getStatus(Long userId, Integer attractionNo) {
        requireAttraction(attractionNo);
        boolean liked = userId != null && likeMapper.exists(userId, attractionNo);
        return new LikeStatusResponse(liked, likeMapper.countByAttraction(attractionNo));
    }

    /** 좋아요 추가(멱등 — 이미 했으면 그대로). */
    @Transactional
    public LikeStatusResponse like(Long userId, Integer attractionNo) {
        requireAttraction(attractionNo);
        likeMapper.insertIgnore(userId, attractionNo);
        return new LikeStatusResponse(true, likeMapper.countByAttraction(attractionNo));
    }

    /** 좋아요 취소(멱등 — 안 했었으면 그대로). */
    @Transactional
    public LikeStatusResponse unlike(Long userId, Integer attractionNo) {
        requireAttraction(attractionNo);
        likeMapper.delete(userId, attractionNo);
        return new LikeStatusResponse(false, likeMapper.countByAttraction(attractionNo));
    }

    private void requireAttraction(Integer attractionNo) {
        if (!likeMapper.existsAttraction(attractionNo)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "관광지를 찾을 수 없습니다.");
        }
    }
}
