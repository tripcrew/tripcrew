package com.tripcrew.upload.dto;

import java.util.List;

/**
 * 이미지 업로드 응답. 저장된 파일들의 공개 상대 URL 목록을 첨부 순서대로 돌려준다.
 * 프론트는 이 URL 들을 후기 작성/수정 요청({@code imageUrls})에 그대로 실어 보낸다.
 */
public record UploadResponse(List<String> urls) {
}
