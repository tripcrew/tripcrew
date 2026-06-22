package com.tripcrew.notice.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.notice.model.dto.Notice;
import com.tripcrew.notice.model.dto.NoticeCreateRequest;
import com.tripcrew.notice.model.dto.NoticeResponse;
import com.tripcrew.notice.model.dto.NoticeUpdateRequest;
import com.tripcrew.notice.model.mapper.NoticeMapper;

import lombok.RequiredArgsConstructor;

/**
 * F10 공지사항. 목록/상세 조회는 공개(SecurityConfig permitAll), 작성/수정/삭제는 관리자 전용
 * (/api/admin/** → ROLE_ADMIN). 상세 조회 시 조회수를 +1 한다.
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    /** 공개 목록(고정 우선·최신순). */
    @Transactional(readOnly = true)
    public List<NoticeResponse> list() {
        return noticeMapper.findAll().stream()
                .map(NoticeResponse::from)
                .toList();
    }

    /**
     * 공개 상세. 조회할 때마다 조회수를 +1 한 뒤, 증가된 값으로 응답한다.
     * (조회수 어뷰징 방지는 범위 밖 — 단순 증가)
     */
    @Transactional
    public NoticeResponse getAndIncreaseView(Long id) {
        int affected = noticeMapper.incrementViewCount(id);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다.");
        }
        return NoticeResponse.from(findOrThrow(id));
    }

    /** 공지 작성(관리자). 작성자는 인증 주체. */
    @Transactional
    public NoticeResponse create(Long adminId, NoticeCreateRequest request) {
        Notice notice = Notice.builder()
                .authorId(adminId)
                .title(request.title())
                .content(request.content())
                .pinned(request.pinned())
                .build();
        noticeMapper.insert(notice);
        return NoticeResponse.from(findOrThrow(notice.getId()));
    }

    /** 공지 수정(관리자). 제목/내용/고정 여부 교체. */
    @Transactional
    public NoticeResponse update(Long id, NoticeUpdateRequest request) {
        Notice notice = Notice.builder()
                .id(id)
                .title(request.title())
                .content(request.content())
                .pinned(request.pinned())
                .build();
        int affected = noticeMapper.update(notice);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다.");
        }
        return NoticeResponse.from(findOrThrow(id));
    }

    /** 공지 삭제(관리자). 없으면 404. */
    @Transactional
    public void delete(Long id) {
        int affected = noticeMapper.deleteById(id);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다.");
        }
    }

    private Notice findOrThrow(Long id) {
        return noticeMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."));
    }
}
