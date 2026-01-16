package com.blog.backend.feature.file.controller;

import com.blog.backend.feature.file.dto.UploadResponse;
import com.blog.backend.feature.file.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 업로드 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class UploadController {

    private final FileMetadataService fileMetadataService;

    /**
     * 파일들을 임시 업로드합니다.
     *
     * 업로드 즉시 S3에 저장되며, postId = NULL 상태로 DB에 저장됩니다.
     * 게시글 저장 시점에 postId가 업데이트됩니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드 응답 (파일 ID, URL 포함)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("파일 업로드 요청: fileName={}, size={}",
                file.getOriginalFilename(), file.getSize());

        UploadResponse response = fileMetadataService.uploadEditorImage(file);

        log.info("파일 업로드 성공: fileId={}, url={}", response.id(), response.url());

        return ResponseEntity.ok(response);
    }
}