package com.blog.backend.global.file.util;

import com.blog.backend.global.core.exception.CustomException;
import com.blog.backend.global.file.constant.FileTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 검증 전용 클래스 (완화된 버전)
 * - 확장자 기반으로만 검증 (MIME 타입 검증 제거)
 * - 컨트롤러 레이어에서 사용
 */
@Slf4j
public class FileValidator {

    /**
     * 파일 업로드 전체 검증
     *
     * 검증 순서:
     * 1. 파일 존재 여부
     * 2. 파일명 유효성
     * 3. 확장자 허용 여부
     * 4. 파일 크기 (확장자 기반 카테고리별)
     *
     * @param file 업로드할 파일
     * @throws CustomException 검증 실패 시
     */
    public static void validateFile(MultipartFile file) {
        // 1. 파일 존재 여부
        validateFileNotEmpty(file);

        // 2. 파일명 유효성
        validateFileName(file.getOriginalFilename());

        String extension = extractExtension(file.getOriginalFilename());

        // 3. 확장자 허용 여부
        validateExtension(extension);

        // 4. 확장자 기반으로 카테고리를 추론하고 파일 크기 검증
        validateFileSize(file.getSize(), extension);

        log.info("파일 검증 완료: filename={}, size={}bytes, extension={}",
                file.getOriginalFilename(), file.getSize(), extension);
    }

    /**
     * 파일이 비어있지 않은지 검증
     */
    private static void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw CustomException.badRequest("파일이 비어있습니다.");
        }
    }

    /**
     * 파일명 검증
     */
    private static void validateFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            throw CustomException.badRequest("파일명을 확인할 수 없습니다.");
        }

        if (filename.length() > 255) {
            throw CustomException.badRequest("파일명이 너무 깁니다. (최대 255자)");
        }

        // 위험한 문자 검증 (경로 조작 방지)
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw CustomException.badRequest("파일명에 허용되지 않는 문자가 포함되어 있습니다.");
        }
    }

    /**
     * 확장자가 허용 목록에 있는지 검증
     */
    private static void validateExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw CustomException.badRequest("파일 확장자를 확인할 수 없습니다.");
        }

        if (!isAllowedExtension(extension)) {
            throw CustomException.badRequest(
                    String.format("지원하지 않는 파일 확장자입니다: .%s", extension)
            );
        }
    }

    /**
     * 확장자 기반으로 파일 카테고리를 추론하고 해당 타입의 크기 제한 검증
     */
    private static void validateFileSize(long fileSize, String extension) {
        if (fileSize <= 0) {
            throw CustomException.badRequest("파일 크기를 확인할 수 없습니다.");
        }

        // 확장자로 파일 카테고리 추론
        String fileTypeName;
        long maxSize;

        if (FileTypeConstants.Image.ALLOWED_EXTENSIONS.contains(extension)) {
            fileTypeName = FileTypeConstants.Image.TYPE_NAME;
            maxSize = FileTypeConstants.Image.MAX_SIZE;
        } else if (FileTypeConstants.Video.ALLOWED_EXTENSIONS.contains(extension)) {
            fileTypeName = FileTypeConstants.Video.TYPE_NAME;
            maxSize = FileTypeConstants.Video.MAX_SIZE;
        } else if (FileTypeConstants.Document.ALLOWED_EXTENSIONS.contains(extension)) {
            fileTypeName = FileTypeConstants.Document.TYPE_NAME;
            maxSize = FileTypeConstants.Document.MAX_SIZE;
        } else if (FileTypeConstants.Audio.ALLOWED_EXTENSIONS.contains(extension)) {
            fileTypeName = FileTypeConstants.Audio.TYPE_NAME;
            maxSize = FileTypeConstants.Audio.MAX_SIZE;
        } else if (FileTypeConstants.Archive.ALLOWED_EXTENSIONS.contains(extension)) {
            fileTypeName = FileTypeConstants.Archive.TYPE_NAME;
            maxSize = FileTypeConstants.Archive.MAX_SIZE;
        } else {
            // 이 시점에서는 이미 허용된 확장자이므로 도달하지 않음
            throw CustomException.badRequest("알 수 없는 파일 타입입니다.");
        }

        // 크기 초과 시 카테고리별 메시지와 함께 예외 발생
        if (fileSize > maxSize) {
            throw CustomException.badRequest(
                    String.format("%s는 최대 %dMB까지 업로드 가능합니다. (현재 파일: %.2fMB)",
                            fileTypeName,
                            maxSize / (1024 * 1024),
                            fileSize / (1024.0 * 1024.0))
            );
        }
    }

    /**
     * 허용된 확장자인지 확인
     */
    private static boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }

        return FileTypeConstants.Image.ALLOWED_EXTENSIONS.contains(extension)
                || FileTypeConstants.Video.ALLOWED_EXTENSIONS.contains(extension)
                || FileTypeConstants.Document.ALLOWED_EXTENSIONS.contains(extension)
                || FileTypeConstants.Audio.ALLOWED_EXTENSIONS.contains(extension)
                || FileTypeConstants.Archive.ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 파일명에서 확장자 추출
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
}