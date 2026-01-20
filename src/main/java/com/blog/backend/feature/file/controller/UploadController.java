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
 *
 * 업로드된 파일의 MIME 타입을 자동으로 분석하여
 * 적절한 디렉토리(images/videos/documents)에 저장합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class UploadController {

    private final FileMetadataService fileMetadataService;

    /**
     * 에디터 파일 임시 업로드 (MIME 타입 기반 자동 분류)
     *
     * 지원 파일:
     * - 이미지: JPG, PNG, GIF, WebP, SVG 등
     * - 비디오: MP4, MOV, AVI, WebM 등
     * - 문서: PDF, Word, Excel, PowerPoint 등
     *
     * 업로드 즉시 S3에 저장되며, 독립 상태로 DB에 저장됩니다.
     * 게시글 저장 시점에 PostFile 매핑이 생성됩니다.
     *
     * @param file 업로드할 파일 (이미지/비디오/문서)
     * @return 업로드 응답 (파일 ID, URL, 타입 포함)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("파일 업로드 요청: fileName={}, contentType={}, size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        UploadResponse response = fileMetadataService.uploadEditorImage(file);

        log.info("파일 업로드 성공: fileId={}, fileType={}, url={}",
                response.id(), response.fileMetadataType(), response.url());

        return ResponseEntity.ok(response);
    }
}