package com.blog.backend.global.file.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 파일 타입별 상수 정의
 * 백엔드 검증에 필요한 최소 상수만 유지
 */
public class FileTypeConstants {

    /**
     * 이미지 파일 타입 정의
     */
    public static class Image {
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp"
        );
        public static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
        public static final String TYPE_NAME = "이미지";
    }

    /**
     * 비디오 파일 타입 정의
     */
    public static class Video {
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "mp4", "mpeg", "mov", "avi", "flv", "webm", "mkv"
        );
        public static final long MAX_SIZE = 100 * 1024 * 1024; // 100MB
        public static final String TYPE_NAME = "비디오";
    }

    /**
     * 문서 파일 타입 정의
     */
    public static class Document {
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
        );
        public static final long MAX_SIZE = 20 * 1024 * 1024; // 20MB
        public static final String TYPE_NAME = "문서";
    }

    /**
     * 오디오 파일 타입 정의
     */
    public static class Audio {
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "mp3", "wav", "ogg", "flac", "aac", "m4a"
        );
        public static final long MAX_SIZE = 20 * 1024 * 1024; // 20MB
        public static final String TYPE_NAME = "오디오";
    }

    /**
     * 압축 파일 타입 정의
     */
    public static class Archive {
        public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
                "zip", "rar", "7z", "tar", "gz"
        );
        public static final long MAX_SIZE = 100 * 1024 * 1024; // 100MB
        public static final String TYPE_NAME = "압축 파일";
    }
}