package com.blog.backend.global.file.controller;

import com.blog.backend.global.core.response.ApiResponse;
import com.blog.backend.global.file.dto.FileUploadResponse;
import com.blog.backend.global.file.entity.FileMetadata;
import com.blog.backend.global.file.service.FileMetadataService;
import com.blog.backend.global.file.util.FileValidator;
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
 * - 파일 검증 (크기, 확장자, MIME 타입)
 * - S3 업로드 및 메타데이터 저장
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3FileService s3FileService;
    private final FileMetadataService fileMetadataService;

    /**
     * 파일 업로드
     *
     * 처리 흐름:
     * 1. 파일 검증 (크기, 확장자, MIME 타입)
     * 2. S3에 파일 업로드 (타입별 경로 자동 분류)
     * 3. FileMetadata 생성 및 저장
     * 4. 업로드 결과 반환
     *
     * @param file 업로드할 파일
     * @return FileUploadResponse 업로드된 파일 정보 (ID, URL 등)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("파일 업로드 요청: filename={}, contentType={}, size={}bytes",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        // 1. 파일 검증
        FileValidator.validateFile(file);
        log.info("파일 검증 완료: filename={}", file.getOriginalFilename());

        // 2. S3 업로드 (타입별 경로 자동 분류)
        S3UploadResult uploadResult = s3FileService.uploadFile(file);
        log.info("S3 업로드 완료: s3Key={}, url={}", uploadResult.s3Key(), uploadResult.url());

        // 3. FileMetadata 저장
        FileMetadata fileMetadata = fileMetadataService.saveFileMetadata(uploadResult);
        log.info("파일 메타데이터 저장 완료: fileId={}", fileMetadata.getId());

        // 4. 응답 반환
        FileUploadResponse response = FileUploadResponse.from(fileMetadata);
        log.info("파일 업로드 성공: fileId={}, url={}", response.id(), response.url());

        return ResponseEntity.ok(ApiResponse.success(response, "파일이 업로드되었습니다"));
    }
}