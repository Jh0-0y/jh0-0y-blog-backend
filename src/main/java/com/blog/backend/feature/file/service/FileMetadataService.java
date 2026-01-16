package com.blog.backend.feature.file.service;

import com.blog.backend.feature.file.dto.UploadResponse;
import com.blog.backend.feature.file.entity.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 파일 메타데이터 관리 서비스
 */
public interface FileMetadataService {

    /**
     * 에디터 이미지를 업로드합니다 (독립 상태).
     * PostFile 매핑은 게시글 저장 시점에 생성됩니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드 응답 (파일 ID, URL 포함)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    UploadResponse uploadEditorImage(MultipartFile file) throws IOException;

    /**
     * 고아 파일(어떤 매핑 테이블에도 존재하지 않고 생성 후 일정 시간 경과)을 삭제합니다.
     *
     * @param hoursThreshold 기준 시간 (예: 24시간)
     * @return 삭제된 파일 개수
     */
    int deleteOrphanFiles(int hoursThreshold);

    /**
     * 파일 메타데이터를 조회합니다.
     *
     * @param fileId 파일 ID
     * @return 파일 메타데이터
     */
    FileMetadata getFileMetadata(Long fileId);

    /**
     * 여러 파일 ID가 모두 존재하는지 검증합니다.
     *
     * @param fileIds 검증할 파일 ID 목록
     * @throws com.blog.backend.global.error.CustomException 일부 파일이 존재하지 않을 경우
     */
    void validateFilesExist(List<Long> fileIds);
}