package com.blog.backend.infra.s3.util;

import com.blog.backend.infra.s3.constant.FileTypeConstants;
import com.blog.backend.infra.s3.constant.S3StoragePath;
import com.blog.backend.infra.s3.exception.S3CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * 파일 확장자와 MIME 타입을 분석하여 적절한 S3StoragePath를 결정하는 유틸리티
 */
@Slf4j
public class S3FileTypeResolver {

    /**
     * 파일명과 MIME 타입을 기반으로 S3StoragePath를 결정합니다.
     * 확장자와 MIME 타입 둘 다 검증하여 더 안전한 파일 업로드를 보장합니다.
     *
     * @param filename    파일명
     * @param contentType 파일의 MIME 타입
     * @return 결정된 S3StoragePath
     * @throws S3CustomException 지원하지 않는 파일 타입인 경우
     */
    public static S3StoragePath resolveStoragePath(String filename, String contentType) {
        // 입력 검증
        if (filename == null || filename.isBlank()) {
            log.warn("파일명이 null 또는 비어있음");
            throw new S3CustomException("파일명을 확인할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if (contentType == null || contentType.isBlank()) {
            log.warn("MIME 타입이 null 또는 비어있음: filename={}", filename);
            throw new S3CustomException("파일 타입을 확인할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 확장자 및 MIME 타입 추출
        String extension = extractExtension(filename);
        String normalizedMimeType = normalizeMimeType(contentType);

        log.info("파일 타입 분석 중: filename={}, extension={}, mimeType={}",
                filename, extension, normalizedMimeType);

        // 이미지 파일 검증
        if (isImage(extension, normalizedMimeType)) {
            log.info("이미지 파일로 분류: {}", filename);
            return S3StoragePath.PUBLIC_IMAGE;
        }

        // 비디오 파일 검증
        if (isVideo(extension, normalizedMimeType)) {
            log.info("비디오 파일로 분류: {}", filename);
            return S3StoragePath.PUBLIC_VIDEO;
        }

        // 문서 파일 검증
        if (isDocument(extension, normalizedMimeType)) {
            log.info("문서 파일로 분류: {}", filename);
            return S3StoragePath.PUBLIC_DOCUMENT;
        }

        // 오디오 파일 검증
        if (isAudio(extension, normalizedMimeType)) {
            log.info("오디오 파일로 분류: {}", filename);
            return S3StoragePath.PUBLIC_AUDIO;
        }

        // 압축 파일 검증
        if (isArchive(extension, normalizedMimeType)) {
            log.info("압축 파일로 분류: {}", filename);
            return S3StoragePath.PUBLIC_ARCHIVE;
        }

        // 지원하지 않는 파일 타입
        log.error("지원하지 않는 파일 타입: filename={}, extension={}, mimeType={}",
                filename, extension, normalizedMimeType);
        throw S3CustomException.badRequest(
                String.format("지원하지 않는 파일 형식입니다. (확장자: %s, MIME: %s)", extension, normalizedMimeType)
        );
    }

    /**
     * 파일이 이미지인지 확인 (확장자와 MIME 타입 모두 검증)
     */
    public static boolean isImage(String extension, String mimeType) {
        boolean extensionMatch = FileTypeConstants.Image.ALLOWED_EXTENSIONS.contains(extension);
        boolean mimeTypeMatch = FileTypeConstants.Image.ALLOWED_MIME_TYPES.contains(mimeType);
        return extensionMatch && mimeTypeMatch;
    }

    /**
     * 파일이 비디오인지 확인 (확장자와 MIME 타입 모두 검증)
     */
    public static boolean isVideo(String extension, String mimeType) {
        boolean extensionMatch = FileTypeConstants.Video.ALLOWED_EXTENSIONS.contains(extension);
        boolean mimeTypeMatch = FileTypeConstants.Video.ALLOWED_MIME_TYPES.contains(mimeType);
        return extensionMatch && mimeTypeMatch;
    }

    /**
     * 파일이 문서인지 확인 (확장자와 MIME 타입 모두 검증)
     */
    public static boolean isDocument(String extension, String mimeType) {
        boolean extensionMatch = FileTypeConstants.Document.ALLOWED_EXTENSIONS.contains(extension);
        boolean mimeTypeMatch = FileTypeConstants.Document.ALLOWED_MIME_TYPES.contains(mimeType);
        return extensionMatch && mimeTypeMatch;
    }

    /**
     * 파일이 오디오인지 확인 (확장자와 MIME 타입 모두 검증)
     */
    public static boolean isAudio(String extension, String mimeType) {
        boolean extensionMatch = FileTypeConstants.Audio.ALLOWED_EXTENSIONS.contains(extension);
        boolean mimeTypeMatch = FileTypeConstants.Audio.ALLOWED_MIME_TYPES.contains(mimeType);
        return extensionMatch && mimeTypeMatch;
    }

    /**
     * 파일이 압축 파일인지 확인 (확장자와 MIME 타입 모두 검증)
     */
    public static boolean isArchive(String extension, String mimeType) {
        boolean extensionMatch = FileTypeConstants.Archive.ALLOWED_EXTENSIONS.contains(extension);
        boolean mimeTypeMatch = FileTypeConstants.Archive.ALLOWED_MIME_TYPES.contains(mimeType);
        return extensionMatch && mimeTypeMatch;
    }

    /**
     * 허용된 파일 타입인지 확인 (확장자 기반)
     */
    public static boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }

        String normalizedExt = extension.toLowerCase();
        return FileTypeConstants.Image.ALLOWED_EXTENSIONS.contains(normalizedExt)
                || FileTypeConstants.Video.ALLOWED_EXTENSIONS.contains(normalizedExt)
                || FileTypeConstants.Document.ALLOWED_EXTENSIONS.contains(normalizedExt)
                || FileTypeConstants.Audio.ALLOWED_EXTENSIONS.contains(normalizedExt)
                || FileTypeConstants.Archive.ALLOWED_EXTENSIONS.contains(normalizedExt);
    }

    /**
     * 허용된 파일 타입인지 확인 (MIME 타입 기반)
     */
    public static boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }

        String normalizedMime = normalizeMimeType(mimeType);
        return FileTypeConstants.Image.ALLOWED_MIME_TYPES.contains(normalizedMime)
                || FileTypeConstants.Video.ALLOWED_MIME_TYPES.contains(normalizedMime)
                || FileTypeConstants.Document.ALLOWED_MIME_TYPES.contains(normalizedMime)
                || FileTypeConstants.Audio.ALLOWED_MIME_TYPES.contains(normalizedMime)
                || FileTypeConstants.Archive.ALLOWED_MIME_TYPES.contains(normalizedMime);
    }

    /**
     * 파일명과 MIME 타입 기반으로 허용된 파일인지 확인
     */
    public static boolean isAllowedFile(String filename, String contentType) {
        String extension = extractExtension(filename);
        String normalizedMime = normalizeMimeType(contentType);

        return isAllowedExtension(extension) && isAllowedMimeType(normalizedMime);
    }


    /**
     * 파일명에서 확장자를 추출합니다.
     *
     * @param filename 파일명
     * @return 확장자 (소문자, 점 제외)
     */
    private static String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * MIME 타입을 정규화합니다 (소문자 변환, 파라미터 제거).
     *
     * @param contentType 원본 MIME 타입
     * @return 정규화된 MIME 타입
     */
    private static String normalizeMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        return contentType.toLowerCase().split(";")[0].trim();
    }
}