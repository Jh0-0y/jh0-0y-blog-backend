package com.blog.backend.feature.post.controller;

import com.blog.backend.feature.post.dto.PostFileUploadResponse;
import com.blog.backend.global.file.entity.FileMetadata;
import com.blog.backend.global.file.service.FileMetadataService;
import com.blog.backend.infra.s3.dto.S3UploadResult;
import com.blog.backend.infra.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 업로드 컨트롤러
 *
 * 비즈니스 로직:
 * 1. S3에 파일 업로드
 * 2. 업로드 결과로 FileMetadata 생성 및 저장
 * 3. 응답 반환
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class PostFileUploadController {

    private final S3FileService s3FileService;
    private final FileMetadataService fileMetadataService;

    /**
     * 파일 업로드 (MIME 타입 기반 자동 분류)
     *
     * 처리 흐름:
     * 1. S3에 파일 업로드 (자동 타입 분류)
     * 2. S3UploadResult 반환
     * 3. FileMetadata 생성 및 저장
     * 4. UploadResponse 반환
     *
     * @param file 업로드할 파일
     * @return 업로드 응답 (파일 ID, URL 포함)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<PostFileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("파일 업로드 요청: fileName={}, contentType={}, size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        // 1. S3에 파일 업로드 (자동 타입 분류 및 검증)
        S3UploadResult uploadResult = s3FileService.uploadFile(file);
        log.info("S3 업로드 완료: s3Key={}, url={}", uploadResult.s3Key(), uploadResult.url());

        // 2. FileMetadata 생성 및 저장
        FileMetadata fileMetadata = fileMetadataService.saveFileMetadata(uploadResult);
        log.info("파일 메타데이터 저장 완료: fileId={}", fileMetadata.getId());

        // 3. 응답 반환
        PostFileUploadResponse response = PostFileUploadResponse.from(fileMetadata);
        log.info("파일 업로드 성공: fileId={}, url={}", response.id(), response.url());

        return ResponseEntity.ok(response);
    }
}