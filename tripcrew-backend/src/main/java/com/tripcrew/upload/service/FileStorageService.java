package com.tripcrew.upload.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tripcrew.common.exception.BusinessException;

import jakarta.annotation.PostConstruct;

/**
 * 로컬 파일시스템 기반 이미지 저장소(F08 후기 이미지).
 *
 * <p>업로드 파일은 {@code app.upload.dir}(기본 {@code uploads}) 아래 하위 폴더별로 저장하고,
 * 정적 리소스로 노출되는 상대 URL({@code /uploads/{sub}/{uuid}.ext})을 돌려준다.
 * 운영에서 S3 로 바꾸더라도 이 서비스의 인터페이스(store/delete + 상대 URL 반환)만 유지하면 된다.
 *
 * <p>도커에서는 작업 디렉터리 {@code /app} 가 호스트의 {@code tripcrew-backend} 에 바인드 마운트되어
 * 업로드 파일이 컨테이너 재시작에도 보존된다.
 */
@Service
public class FileStorageService {

    /** 정적 리소스로 노출되는 URL 접두사. SecurityConfig·WebMvcConfig 와 맞춰야 한다. */
    public static final String PUBLIC_PREFIX = "/uploads/";

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB / 파일
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리를 만들 수 없습니다: " + uploadRoot, e);
        }
    }

    /**
     * 이미지 한 장을 {@code subDir} 하위에 저장하고 공개 상대 URL 을 반환한다.
     *
     * @param subDir 하위 폴더(예: "reviews")
     * @return {@code /uploads/{subDir}/{uuid}.ext}
     */
    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미지 한 장은 10MB 이하만 업로드할 수 있습니다.");
        }
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미지 파일(JPG/PNG/WEBP/GIF)만 업로드할 수 있습니다.");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = uploadRoot.resolve(subDir).normalize();
        try {
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했습니다.");
        }
        return PUBLIC_PREFIX + subDir + "/" + filename;
    }

    /**
     * 공개 URL 에 해당하는 실제 파일을 삭제한다(best-effort, 없거나 범위 밖이면 무시).
     * 외부 URL·경로 탈출 시도는 무시한다.
     */
    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        String relative = publicUrl.substring(PUBLIC_PREFIX.length());
        Path target = uploadRoot.resolve(relative).normalize();
        if (!target.startsWith(uploadRoot)) {
            return; // 경로 탈출 방지
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 파일 정리는 실패해도 비즈니스 흐름을 막지 않는다.
        }
    }
}
