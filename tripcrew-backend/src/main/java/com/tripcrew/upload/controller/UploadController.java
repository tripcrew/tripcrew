package com.tripcrew.upload.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.upload.dto.UploadResponse;
import com.tripcrew.upload.service.FileStorageService;

import lombok.RequiredArgsConstructor;

/**
 * 이미지 업로드(F08 후기 첨부). 인증 필요(SecurityConfig anyRequest authenticated 로 커버).
 *
 * <p>후기 작성/수정과 분리된 2단계 흐름: 먼저 이곳에 파일을 올려 공개 URL 을 받고,
 * 그 URL 들을 후기 작성/수정 요청({@code imageUrls})에 실어 보낸다.
 * 후기 작성 전 미리보기가 가능하고 기존 JSON 후기 API 를 건드리지 않는다.
 */
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    /** 한 번에 올릴 수 있는 이미지 수(후기당 최대 첨부 수와 동일). */
    private static final int MAX_IMAGES = 5;
    private static final String REVIEW_SUBDIR = "reviews";

    private final FileStorageService fileStorageService;

    /** 후기용 이미지 업로드. multipart 파트 이름은 {@code files}. */
    @PostMapping("/images")
    public UploadResponse uploadImages(@AuthenticationPrincipal Long userId,
                                       @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "업로드할 이미지를 선택해주세요.");
        }
        if (files.size() > MAX_IMAGES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미지는 최대 " + MAX_IMAGES + "장까지 올릴 수 있습니다.");
        }
        List<String> urls = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            urls.add(fileStorageService.store(file, REVIEW_SUBDIR));
        }
        return new UploadResponse(urls);
    }
}
