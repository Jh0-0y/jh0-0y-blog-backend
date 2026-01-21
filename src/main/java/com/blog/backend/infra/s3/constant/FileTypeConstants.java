package com.blog.backend.infra.s3.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 파일 타입별 허용 확장자 및 MIME 타입 상수
 */
public class FileTypeConstants {

    /**
     * 이미지 파일 타입 정의
     */
    public static class Image {
        // 허용 확장자
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp"
        );

        // 허용 MIME 타입
        public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/webp",
                "image/svg+xml",
                "image/bmp"
        );
    }

    /**
     * 비디오 파일 타입 정의
     */
    public static class Video {
        // 허용 확장자
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "mp4", "mpeg", "mov", "avi", "flv", "webm", "mkv"
        );

        // 허용 MIME 타입
        public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
                "video/mp4",
                "video/mpeg",
                "video/quicktime",
                "video/x-msvideo",
                "video/x-flv",
                "video/webm",
                "video/x-matroska"
        );
    }

    /**
     * 문서 파일 타입 정의
     */
    public static class Document {
        // 허용 확장자
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
        );

        // 허용 MIME 타입
        public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
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
    }

    /**
     * 오디오 파일 타입 정의
     */
    public static class Audio {
        // 허용 확장자
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "mp3", "wav", "ogg", "flac", "aac", "m4a"
        );

        // 허용 MIME 타입
        public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
                "audio/mpeg",
                "audio/wav",
                "audio/ogg",
                "audio/flac",
                "audio/aac",
                "audio/x-m4a"
        );
    }

    /**
     * 압축 파일 타입 정의
     */
    public static class Archive {
        // 허용 확장자
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "zip", "rar", "7z", "tar", "gz"
        );

        // 허용 MIME 타입
        public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
                "application/zip",
                "application/x-rar-compressed",
                "application/x-7z-compressed",
                "application/x-tar",
                "application/gzip"
        );
    }
}