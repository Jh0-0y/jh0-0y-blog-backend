package com.blog.backend.infra.s3;

import com.blog.backend.feature.file.entity.FileMetadata;
import com.blog.backend.feature.file.entity.FileMetadataType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * S3 파일 업로드/조회/삭제 기능을 제공하는 서비스 인터페이스
 */
public interface S3FileService {

    /**
     * 사용자 프로필 이미지를 S3에 업로드합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param userId 사용자 ID (파일명 생성용)
     * @return 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileMetadata uploadProfileImage(MultipartFile file, Long userId) throws IOException;

    /* ========= 블로그 ========= */

    /**
     * 블로그 썸네일 이미지를 S3에 업로드합니다.
     *
     * @param file 업로드할 썸네일 이미지 파일
     * @param postId 게시글 ID (파일명 생성 및 게시글 연결용)
     * @return 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileMetadata uploadBlogThumbnail(MultipartFile file, Long postId) throws IOException;

    /**
     * 블로그 에디터 이미지를 S3에 업로드합니다.
     * 에디터에서 드래그 시 즉시 업로드되며, postId는 NULL 상태로 저장됩니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 파일 메타데이터 (postId = NULL)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileMetadata uploadBlogImage(MultipartFile file) throws IOException;

    /**
     * 블로그 동영상 파일을 S3에 업로드합니다.
     *
     * @param file 업로드할 동영상 파일
     * @return 업로드된 파일 메타데이터 (postId = NULL)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileMetadata uploadBlogVideo(MultipartFile file) throws IOException;

    /* ======== 범용 업로드 메서드 ========= */

    /**
     * 파일 타입에 따라 지정된 S3 디렉토리에 파일을 업로드합니다.
     *
     * @param multipartFile 업로드할 파일
     * @param fileMetadataType 파일 분류 (IMAGE, THUMBNAIL, VIDEO, PROFILE_IMAGE 등)
     * @return 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileMetadata uploadFile(
            MultipartFile multipartFile,
            FileMetadataType fileMetadataType
    ) throws IOException;

    /* =========== 기타 유틸 ============ */

    /**
     * S3 공개 경로(public)에 업로드된 파일의 Public URL을 반환합니다.
     *
     * @param fileId 조회할 파일 ID
     * @return S3 Public URL 또는 CloudFront URL
     */
    String getPublicUrl(Long fileId);

    /**
     * S3 파일에 대해 일정 시간만 접근 가능한 Presigned URL을 생성합니다.
     * (동영상 등 보안이 필요한 파일에 사용)
     *
     * @param fileId 조회할 파일 ID
     * @param minutes URL 유효 시간(분)
     * @return Presigned URL
     */
    String getPresignedUrl(Long fileId, int minutes);

    /**
     * S3에 업로드된 파일을 삭제하고 DB에서도 파일 메타데이터를 제거합니다.
     *
     * @param fileMetadata 삭제할 파일 메타데이터 엔티티
     */
    void deleteFile(FileMetadata fileMetadata);
}