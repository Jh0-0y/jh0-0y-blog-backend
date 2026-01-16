package com.blog.backend.infra.s3;

import com.blog.backend.feature.file.entity.FileMetadata;
import com.blog.backend.feature.file.entity.FileMetadataType;
import com.blog.backend.feature.file.repository.FileMetadataRepository;
import com.blog.backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * S3 파일 업로드/조회/삭제 기능을 제공하는 서비스 구현체
 *
 * 리팩터링 핵심:
 * - postId 파라미터 제거 (독립적인 파일 관리)
 * - FileMetadata는 업로드 즉시 생성, 매핑은 별도 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class S3FileServiceImpl implements S3FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    @Override
    public FileMetadata uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        return uploadFileWithIdentifier(file, FileMetadataType.PROFILE_IMAGE, userId);
    }

    @Override
    public FileMetadata uploadBlogThumbnail(MultipartFile file, Long postId) throws IOException {
        return uploadFileWithIdentifier(file, FileMetadataType.THUMBNAIL, postId);
    }

    @Override
    public FileMetadata uploadBlogImage(MultipartFile file) throws IOException {
        return uploadFileInternal(file, FileMetadataType.IMAGE);
    }

    @Override
    public FileMetadata uploadBlogVideo(MultipartFile file) throws IOException {
        return uploadFileInternal(file, FileMetadataType.VIDEO);
    }

    @Override
    public FileMetadata uploadFile(MultipartFile multipartFile, FileMetadataType fileMetadataType) throws IOException {
        return uploadFileInternal(multipartFile, fileMetadataType);
    }

    /**
     * 파일을 S3에 업로드하고 메타데이터를 저장합니다.
     * (독립 상태로 저장, 매핑 테이블 연결은 별도 처리)
     */
    private FileMetadata uploadFileInternal(
            MultipartFile multipartFile,
            FileMetadataType fileMetadataType
    ) throws IOException {
        return uploadFileWithIdentifier(multipartFile, fileMetadataType, null);
    }

    /**
     * identifier를 사용하여 파일을 S3에 업로드합니다.
     * (썸네일, 프로필 이미지 등 고정 파일명이 필요한 경우)
     */
    private FileMetadata uploadFileWithIdentifier(
            MultipartFile multipartFile,
            FileMetadataType fileMetadataType,
            Long identifier
    ) throws IOException {
        // 파일 유효성 검증
        validateFile(multipartFile);

        try {
            // 1. 확장자 추출
            String extension = extractExtension(multipartFile.getOriginalFilename());

            // 2. 파일명 생성 (UUID 또는 고정명)
            String fileName = fileMetadataType.generateFileName(identifier, extension);

            // 3. S3 전체 경로(Key) 생성
            String s3Key = fileMetadataType.getFullPath() + "/" + fileName;

            // 4. S3 업로드 요청 생성 (contentType 명시)
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(multipartFile.getContentType())
                    .build();

            // 5. S3에 파일 업로드
            s3Client.putObject(putRequest, RequestBody.fromBytes(multipartFile.getBytes()));
            log.info("S3 파일 업로드 성공: bucket={}, key={}", bucketName, s3Key);

            // 6. 공개 URL 생성
            String publicUrl = generatePublicUrl(s3Key);

            // 7. DB에 파일 메타데이터 저장 (독립 상태)
            return saveFileMetadata(multipartFile, fileName, s3Key, publicUrl, fileMetadataType);

        } catch (S3Exception e) {
            log.error("S3 업로드 실패: bucket={}, error={}", bucketName, e.awsErrorDetails().errorMessage(), e);
            throw new CustomException("S3 업로드 중 오류 발생: " + e.awsErrorDetails().errorMessage(), INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("파일 처리 실패: {}", e.getMessage(), e);
            throw new CustomException("파일 읽기 실패: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException("예상치 못한 오류 발생: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getPublicUrl(Long fileId) {
        FileMetadata file = getFileById(fileId);

        // VIDEO 타입은 Presigned URL 사용 권장 (보안상)
        if (file.getFileMetadataType() == FileMetadataType.VIDEO) {
            log.warn("VIDEO 파일에 대한 Public URL 요청: fileId={}", fileId);
            throw new CustomException("동영상 파일은 Public URL을 제공하지 않습니다. Presigned URL을 사용하세요.", INTERNAL_SERVER_ERROR);
        }

        log.info("Public URL 조회 완료: fileId={}", fileId);
        return file.getUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public String getPresignedUrl(Long fileId, int minutes) {
        FileMetadata file = getFileById(fileId);

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getS3Key())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(minutes))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presigned.url().toString();

            log.info("Presigned URL 생성 완료: fileId={}, 유효시간={}분", fileId, minutes);
            return presignedUrl;

        } catch (S3Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.awsErrorDetails().errorMessage(), e);
            throw new CustomException("Presigned URL 생성 실패: " + e.awsErrorDetails().errorMessage(), INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new CustomException("Presigned URL 생성 실패: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(FileMetadata file) {
        if (file == null) {
            log.warn("삭제할 파일이 null입니다");
            return;
        }

        try {
            // 1. S3 삭제 요청 생성
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getS3Key())
                    .build();

            // 2. S3에서 파일 삭제
            s3Client.deleteObject(deleteRequest);
            log.info("S3 파일 삭제 성공: bucket={}, key={}", bucketName, file.getS3Key());

            // 3. DB에서 파일 메타데이터 삭제
            fileMetadataRepository.delete(file);
            log.info("DB 파일 메타데이터 삭제 완료: fileId={}", file.getId());

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: key={}, error={}", file.getS3Key(), e.awsErrorDetails().errorMessage(), e);
            throw new CustomException("S3 파일 삭제 실패: " + file.getS3Key(), INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("파일 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new CustomException("파일 삭제 실패: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    // ===== 내부 헬퍼 메서드 =====

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("업로드 파일이 null 또는 비어있음");
            throw new CustomException("업로드할 파일이 없습니다.", INTERNAL_SERVER_ERROR);
        }

        // 파일 크기 체크 (100MB 제한)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            log.warn("파일 크기 초과: {} bytes (최대: {} bytes)", file.getSize(), maxSize);
            throw new CustomException("파일 크기가 100MB를 초과합니다.", INTERNAL_SERVER_ERROR);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            log.warn("파일명이 유효하지 않음");
            throw new CustomException("파일명이 유효하지 않습니다.", INTERNAL_SERVER_ERROR);
        }

        if (!originalName.contains(".")) {
            log.warn("파일 확장자가 없음: {}", originalName);
            throw new CustomException("파일 확장자가 없습니다.", INTERNAL_SERVER_ERROR);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private String generatePublicUrl(String s3Key) {
        if (cloudFrontDomain != null && !cloudFrontDomain.isBlank()) {
            return String.format("https://%s/%s", cloudFrontDomain, s3Key);
        } else {
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        }
    }

    /**
     * 파일 메타데이터를 DB에 저장 (독립 상태)
     */
    private FileMetadata saveFileMetadata(
            MultipartFile multipartFile,
            String storedName,
            String s3Key,
            String publicUrl,
            FileMetadataType fileMetadataType
    ) {
        FileMetadata entity = FileMetadata.builder()
                .originalName(multipartFile.getOriginalFilename())
                .storedName(storedName)
                .s3Key(s3Key)
                .url(publicUrl)
                .contentType(multipartFile.getContentType())
                .fileSize(multipartFile.getSize())
                .fileMetadataType(fileMetadataType)
                .build();

        FileMetadata saved = fileMetadataRepository.save(entity);
        log.info("파일 메타데이터 DB 저장 완료: fileId={}", saved.getId());

        return saved;
    }

    private FileMetadata getFileById(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("파일을 찾을 수 없음: fileId={}", fileId);
                    return new CustomException("파일을 찾을 수 없습니다.", NOT_FOUND);
                });
    }
}