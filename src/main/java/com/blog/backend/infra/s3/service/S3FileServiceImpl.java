package com.blog.backend.infra.s3.service;

import com.blog.backend.infra.s3.constant.S3StoragePath;
import com.blog.backend.infra.s3.dto.S3UploadResult;
import com.blog.backend.infra.s3.exception.S3CustomException;
import com.blog.backend.infra.s3.util.S3KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.InvalidationBatch;
import software.amazon.awssdk.services.cloudfront.model.Paths;
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

import static com.blog.backend.infra.s3.util.S3FileTypeResolver.*;

/**
 * S3 파일 업로드/조회/삭제 기능을 제공하는 서비스 구현체
 *
 * 핵심 특징:
 * - FileMetadata 의존성 완전 제거 (독립적인 S3 비즈니스 로직)
 * - CloudFront 캐시 무효화 처리
 * - S3UploadResult DTO를 통한 데이터 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final CloudFrontClient cloudFrontClient;
    private final S3KeyGenerator s3KeyGenerator;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    @Value("${spring.cloud.aws.cloudfront.distribution-id:}")
    private String distributionId;

    @Override
    public S3UploadResult uploadPublicImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return uploadFileInternal(file, S3StoragePath.PUBLIC_IMAGE);
    }

    @Override
    public S3UploadResult uploadPublicVideo(MultipartFile file) throws IOException {
        validateVideoFile(file);
        return uploadFileInternal(file, S3StoragePath.PUBLIC_VIDEO);
    }

    @Override
    public S3UploadResult uploadPublicDocument(MultipartFile file) throws IOException {
        validateDocumentFile(file);
        return uploadFileInternal(file, S3StoragePath.PUBLIC_DOCUMENT);
    }

    @Override
    public S3UploadResult uploadPublicAudio(MultipartFile file) throws IOException {
        validateAudioFile(file);
        return uploadFileInternal(file, S3StoragePath.PUBLIC_AUDIO);
    }

    @Override
    public S3UploadResult uploadPublicArchive(MultipartFile file) throws IOException {
        validateArchiveFile(file);
        return uploadFileInternal(file, S3StoragePath.PUBLIC_ARCHIVE);
    }

    @Override
    public S3UploadResult uploadPublicAssets(MultipartFile file) throws IOException {
        // Assets는 별도 검증 없이 업로드 (시스템 내부 자원)
        return uploadFileInternal(file, S3StoragePath.PUBLIC_ASSET);
    }

    @Override
    public S3UploadResult uploadFile(MultipartFile file) throws IOException {
        S3StoragePath storagePath = resolveStoragePath(
                file.getOriginalFilename(),
                file.getContentType()
        );
        return uploadFileInternal(file, storagePath);
    }

    @Override
    public String getPresignedUrl(String s3Key, int minutes) {
        if (s3Key == null || s3Key.isBlank()) {
            log.warn("S3 Key가 null 또는 비어있음");
            throw S3CustomException.badRequest("S3 Key가 유효하지 않습니다.");
        }

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(minutes))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presigned.url().toString();

            log.info("Presigned URL 생성 완료: s3Key={}, 유효시간={}분", s3Key, minutes);
            return presignedUrl;

        } catch (S3Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.awsErrorDetails().errorMessage(), e);
            throw S3CustomException.badRequest("Presigned URL 생성 실패: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Presigned URL 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw S3CustomException.badRequest("Presigned URL 생성 실패: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            log.warn("S3 Key가 null 또는 비어있음");
            return;
        }

        try {
            // 1. S3에서 파일 삭제
            deleteFromS3(s3Key);

            // 2. CloudFront 캐시 무효화
            invalidateCloudFrontCache(s3Key);

            log.info("파일 삭제 및 캐시 무효화 완료: s3Key={}", s3Key);

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: key={}, error={}", s3Key, e.awsErrorDetails().errorMessage(), e);
            throw S3CustomException.badRequest("S3 파일 삭제 실패: " + s3Key);
        } catch (Exception e) {
            log.error("파일 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw S3CustomException.badRequest("파일 삭제 실패: " + e.getMessage());
        }
    }

    /* ========== Private 메서드 ============ */

    /**
     * 파일을 S3에 업로드하고 메타데이터 반환
     */
    private S3UploadResult uploadFileInternal(
            MultipartFile file,
            S3StoragePath storagePath
    ) throws IOException {

        validateFile(file);

        try {
            // 1. S3 Key 생성 (경로 + UUID 파일명)
            String s3Key = s3KeyGenerator.generateS3Key(storagePath, file.getOriginalFilename());

            // 2. 저장될 파일명 추출 (UUID 파일명)
            String storedName = extractStoredName(s3Key);

            // 3. S3 업로드 요청 생성
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            // 4. S3에 파일 업로드
            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("S3 파일 업로드 성공: bucket={}, key={}", bucketName, s3Key);

            // 5. 공개 URL 생성 (CloudFront 우선)
            String publicUrl = generatePublicUrl(s3Key);

            // 6. S3UploadResult 반환 (비즈니스 로직에서 DB 저장 처리)
            return S3UploadResult.builder()
                    .originalName(file.getOriginalFilename())
                    .storedName(storedName)
                    .s3Key(s3Key)
                    .url(publicUrl)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (S3Exception e) {
            log.error("S3 업로드 실패: bucket={}, error={}", bucketName, e.awsErrorDetails().errorMessage(), e);
            throw S3CustomException.badRequest("S3 업로드 중 오류 발생: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            log.error("파일 처리 실패: {}", e.getMessage(), e);
            throw S3CustomException.badRequest("파일 읽기 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw S3CustomException.badRequest("예상치 못한 오류 발생: " + e.getMessage());
        }
    }

    /**
     * S3에서 파일 삭제
     */
    private void deleteFromS3(String s3Key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("S3 파일 삭제 성공: bucket={}, key={}", bucketName, s3Key);
    }

    /**
     * CloudFront 캐시 무효화
     */
    private void invalidateCloudFrontCache(String s3Key) {
        if (distributionId == null || distributionId.isBlank()) {
            log.warn("CloudFront Distribution ID가 설정되지 않아 캐시 무효화를 건너뜁니다.");
            return;
        }

        try {
            String path = "/" + s3Key;

            Paths invalidationPaths = Paths.builder()
                    .items(path)
                    .quantity(1)
                    .build();

            InvalidationBatch batch = InvalidationBatch.builder()
                    .paths(invalidationPaths)
                    .callerReference(String.valueOf(System.currentTimeMillis()))
                    .build();

            CreateInvalidationRequest request = CreateInvalidationRequest.builder()
                    .distributionId(distributionId)
                    .invalidationBatch(batch)
                    .build();

            CreateInvalidationResponse response = cloudFrontClient.createInvalidation(request);
            log.info("CloudFront 캐시 무효화 성공: distributionId={}, invalidationId={}, path={}",
                    distributionId, response.invalidation().id(), path);

        } catch (Exception e) {
            log.error("CloudFront 캐시 무효화 실패: {}", e.getMessage(), e);
            // 캐시 무효화 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    /**
     * 공개 URL 생성 (CloudFront 우선)
     */
    private String generatePublicUrl(String s3Key) {
        if (cloudFrontDomain != null && !cloudFrontDomain.isBlank()) {
            return String.format("https://%s/%s", cloudFrontDomain, s3Key);
        } else {
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        }
    }

    /**
     * S3 Key에서 저장된 파일명 추출
     */
    private String extractStoredName(String s3Key) {
        int lastSlashIndex = s3Key.lastIndexOf('/');
        return lastSlashIndex != -1 ? s3Key.substring(lastSlashIndex + 1) : s3Key;
    }

    /* ========== 파일 검증 메서드 ============ */

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw S3CustomException.badRequest("파일이 비어있습니다.");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw S3CustomException.badRequest("파일명을 확인할 수 없습니다.");
        }
    }

    private void validateImageFile(MultipartFile file) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        String mimeType = normalizeMimeType(file.getContentType());

        if (!isImage(extension, mimeType)) {
            throw S3CustomException.badRequest("이미지 파일이 아닙니다.");
        }
    }

    private void validateVideoFile(MultipartFile file) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        String mimeType = normalizeMimeType(file.getContentType());

        if (!isVideo(extension, mimeType)) {
            throw S3CustomException.badRequest("동영상 파일이 아닙니다.");
        }
    }

    private void validateDocumentFile(MultipartFile file) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        String mimeType = normalizeMimeType(file.getContentType());

        if (!isDocument(extension, mimeType)) {
            throw S3CustomException.badRequest("문서 파일이 아닙니다.");
        }
    }

    private void validateAudioFile(MultipartFile file) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        String mimeType = normalizeMimeType(file.getContentType());

        if (!isAudio(extension, mimeType)) {
            throw S3CustomException.badRequest("오디오 파일이 아닙니다.");
        }
    }

    private void validateArchiveFile(MultipartFile file) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        String mimeType = normalizeMimeType(file.getContentType());

        if (!isArchive(extension, mimeType)) {
            throw S3CustomException.badRequest("압축 파일이 아닙니다.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private String normalizeMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        return contentType.toLowerCase().split(";")[0].trim();
    }
}