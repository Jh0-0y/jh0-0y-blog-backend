package com.blog.backend.feature.file.utils;

import com.blog.backend.feature.file.entity.FileMetadataType;
import com.blog.backend.global.error.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

/**
 * 파일 MIME 타입을 분석하여 적절한 FileMetadataType을 결정하는 유틸리티
 */
@Slf4j
public class FileTypeResolver {

    // 이미지 MIME 타입 목록
    private static final List<String> IMAGE_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            "image/bmp"
    );

    // 비디오 MIME 타입 목록
    private static final List<String> VIDEO_MIME_TYPES = Arrays.asList(
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-flv",
            "video/webm",
            "video/x-matroska"
    );

    // 문서 MIME 타입 목록
    private static final List<String> DOCUMENT_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            "text/plain",
            "text/csv"
    );

    /**
     * MIME 타입을 기반으로 FileMetadataType을 결정합니다.
     *
     * @param contentType 파일의 MIME 타입
     * @return 결정된 FileMetadataType
     * @throws CustomException 지원하지 않는 파일 타입인 경우
     */
    public static FileMetadataType resolveFileType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            log.warn("MIME 타입이 null 또는 비어있음");
            throw new CustomException("파일 타입을 확인할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // MIME 타입 정규화 (소문자 변환, 파라미터 제거)
        String normalizedType = contentType.toLowerCase().split(";")[0].trim();

        log.info("파일 타입 분석 중: contentType={}", normalizedType);

        // 이미지 타입 확인
        if (IMAGE_MIME_TYPES.contains(normalizedType)) {
            log.info("이미지 파일로 분류: {}", normalizedType);
            return FileMetadataType.IMAGE;
        }

        // 비디오 타입 확인
        if (VIDEO_MIME_TYPES.contains(normalizedType)) {
            log.info("비디오 파일로 분류: {}", normalizedType);
            return FileMetadataType.VIDEO;
        }

        // 문서 타입 확인
        if (DOCUMENT_MIME_TYPES.contains(normalizedType)) {
            log.info("문서 파일로 분류: {}", normalizedType);
            return FileMetadataType.DOCUMENT;
        }

        // 지원하지 않는 파일 타입
        log.error("지원하지 않는 파일 타입: {}", normalizedType);
        throw new CustomException(
                "지원하지 않는 파일 형식입니다. (허용: 이미지, 비디오, 문서)",
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 파일이 이미지인지 확인
     */
    public static boolean isImage(String contentType) {
        if (contentType == null) return false;
        String normalized = contentType.toLowerCase().split(";")[0].trim();
        return IMAGE_MIME_TYPES.contains(normalized);
    }

    /**
     * 파일이 비디오인지 확인
     */
    public static boolean isVideo(String contentType) {
        if (contentType == null) return false;
        String normalized = contentType.toLowerCase().split(";")[0].trim();
        return VIDEO_MIME_TYPES.contains(normalized);
    }

    /**
     * 파일이 문서인지 확인
     */
    public static boolean isDocument(String contentType) {
        if (contentType == null) return false;
        String normalized = contentType.toLowerCase().split(";")[0].trim();
        return DOCUMENT_MIME_TYPES.contains(normalized);
    }

    /**
     * 허용된 파일 타입인지 확인
     */
    public static boolean isAllowedFileType(String contentType) {
        return isImage(contentType) || isVideo(contentType) || isDocument(contentType);
    }
}