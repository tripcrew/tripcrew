package com.tripcrew.like.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.like.dto.AttractionLikeCount;
import com.tripcrew.like.dto.LikeStatusResponse;
import com.tripcrew.like.dto.WishlistItemResponse;
import com.tripcrew.like.model.mapper.AttractionLikeMapper;

import lombok.RequiredArgsConstructor;

/**
 * 관광지 좋아요(찜). 한 사용자가 한 관광지에 한 번(유니크), POST=좋아요/DELETE=취소 토글.
 * 좋아요 수는 COUNT 로 집계해 매번 최신값을 함께 돌려준다.
 */
@Service
@RequiredArgsConstructor
public class AttractionLikeService {

    /** 카드 배치 통계 한 번에 조회할 수 있는 관광지 수 상한(남용 방지). */
    private static final int MAX_BATCH_NOS = 100;

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

    /** 내 찜 목록(최근 찜 순). 카드 정보 + 평점 요약 + 총 찜 수 포함. */
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> listMine(Long userId) {
        return likeMapper.findLikedByUser(userId);
    }

    /**
     * 관광지 카드용 배치 좋아요 통계(총 찜 수 + 현재 사용자 찜 여부). nos 가 비면 빈 목록.
     * 과도한 요청은 잘라낸다. 비로그인(userId=null)이면 liked 모두 false.
     */
    @Transactional(readOnly = true)
    public List<AttractionLikeCount> listLikeCounts(Long userId, List<Integer> nos) {
        if (nos == null || nos.isEmpty()) {
            return List.of();
        }
        List<Integer> safe = nos.stream().filter(Objects::nonNull).distinct().limit(MAX_BATCH_NOS).toList();
        if (safe.isEmpty()) {
            return List.of();
        }
        return likeMapper.findLikeCountsByNos(userId, safe);
    }

    private void requireAttraction(Integer attractionNo) {
        if (!likeMapper.existsAttraction(attractionNo)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "관광지를 찾을 수 없습니다.");
        }
    }
}
