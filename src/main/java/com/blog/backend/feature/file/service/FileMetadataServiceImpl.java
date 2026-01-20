package com.blog.backend.feature.file.service;

import com.blog.backend.feature.file.dto.UploadResponse;
import com.blog.backend.feature.file.entity.FileMetadata;
import com.blog.backend.feature.file.entity.FileMetadataType;
import com.blog.backend.feature.file.repository.FileMetadataRepository;
import com.blog.backend.feature.file.utils.FileTypeResolver;
import com.blog.backend.global.error.CustomException;
import com.blog.backend.infra.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 파일 메타데이터 관리 서비스 구현체
 *
 * 리팩터링 핵심:
 * - FileMetadata는 어떤 도메인에도 속하지 않는 독립 엔티티
 * - MIME 타입 기반 자동 분류 (IMAGE, VIDEO, DOCUMENT)
 * - 매핑 테이블(PostFile 등)을 통해서만 연결 관리
 * - 고아 파일은 스케줄러가 일괄 정리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileMetadataServiceImpl implements FileMetadataService {

    private final S3FileService s3FileService;
    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public UploadResponse uploadEditorImage(MultipartFile file) throws IOException {
        log.info("에디터 파일 업로드 시작: fileName={}, contentType={}",
                file.getOriginalFilename(), file.getContentType());

        // MIME 타입 기반 자동 분류
        FileMetadataType fileType = FileTypeResolver.resolveFileType(file.getContentType());

        log.info("파일 타입 분류 완료: fileName={}, fileType={}",
                file.getOriginalFilename(), fileType);

        // S3 업로드 및 DB 저장 (독립 상태)
        FileMetadata fileMetadata = s3FileService.uploadFile(file, fileType);

        log.info("에디터 파일 업로드 완료: fileId={}, fileType={}, url={}",
                fileMetadata.getId(), fileType, fileMetadata.getUrl());

        return UploadResponse.from(fileMetadata);
    }

    @Override
    public int deleteOrphanFiles(int hoursThreshold) {
        log.info("고아 파일 정리 시작: 기준 시간={}시간 전", hoursThreshold);

        // 기준 시간 계산
        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(hoursThreshold);

        // 고아 파일 조회 (NOT EXISTS 쿼리로 최적화)
        List<FileMetadata> orphanFiles = fileMetadataRepository.findOrphanFiles(thresholdTime);

        if (orphanFiles.isEmpty()) {
            log.info("삭제할 고아 파일이 없습니다.");
            return 0;
        }

        log.info("고아 파일 발견: 개수={}", orphanFiles.size());

        // 고아 파일 삭제 (S3 + DB)
        int deletedCount = 0;
        for (FileMetadata file : orphanFiles) {
            try {
                s3FileService.deleteFile(file);
                deletedCount++;
                log.info("고아 파일 삭제 완료: fileId={}, fileType={}, s3Key={}",
                        file.getId(), file.getFileMetadataType(), file.getS3Key());
            } catch (Exception e) {
                log.error("고아 파일 삭제 실패: fileId={}, error={}", file.getId(), e.getMessage(), e);
                // 실패해도 계속 진행 (다음 스케줄에서 재시도)
            }
        }

        log.info("고아 파일 정리 완료: 삭제된 파일={}/{}", deletedCount, orphanFiles.size());

        return deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadata getFileMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("파일 메타데이터를 찾을 수 없음: fileId={}", fileId);
                    return new CustomException("파일을 찾을 수 없습니다.", NOT_FOUND);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public void validateFilesExist(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        long existingCount = fileMetadataRepository.countByIdIn(fileIds);

        if (existingCount != fileIds.size()) {
            log.error("일부 파일을 찾을 수 없음: 요청={}, 존재={}", fileIds.size(), existingCount);
            throw new CustomException("일부 파일을 찾을 수 없습니다.", NOT_FOUND);
        }
    }
}