package com.blog.backend.global.file.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 파일 업로드 설정
 * application.yml에서 설정값 주입
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 파일 타입별 최대 크기 (바이트)
     */
    private final Size maxSize = new Size();

    @Getter
    public static class Size {
        private long image = 10 * 1024 * 1024; // 10MB
        private long video = 100 * 1024 * 1024; // 100MB
        private long document = 20 * 1024 * 1024; // 20MB
        private long audio = 20 * 1024 * 1024; // 20MB
        private long archive = 50 * 1024 * 1024; // 50MB

        public void setImage(long image) {
            this.image = image;
        }

        public void setVideo(long video) {
            this.video = video;
        }

        public void setDocument(long document) {
            this.document = document;
        }

        public void setAudio(long audio) {
            this.audio = audio;
        }

        public void setArchive(long archive) {
            this.archive = archive;
        }
    }
}