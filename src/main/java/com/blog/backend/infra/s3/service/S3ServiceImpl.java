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
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class S3ServiceImpl implements S3Service {

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
        return uploadFileInternal(file, S3StoragePath.PUBLIC_IMAGE);
    }

    @Override
    public S3UploadResult uploadPublicVideo(MultipartFile file) throws IOException {
        return uploadFileInternal(file, S3StoragePath.PUBLIC_VIDEO);
    }

    @Override
    public S3UploadResult uploadPublicDocument(MultipartFile file) throws IOException {
        return uploadFileInternal(file, S3StoragePath.PUBLIC_DOCUMENT);
    }

    @Override
    public S3UploadResult uploadPublicAudio(MultipartFile file) throws IOException {
        return uploadFileInternal(file, S3StoragePath.PUBLIC_AUDIO);
    }

    @Override
    public S3UploadResult uploadPublicArchive(MultipartFile file) throws IOException {
        return uploadFileInternal(file, S3StoragePath.PUBLIC_ARCHIVE);
    }

    @Override
    public S3UploadResult uploadPublicAssets(MultipartFile file) throws IOException {
        return uploadFileInternal(file, S3StoragePath.PUBLIC_ASSET);
    }

    @Override
    public S3UploadResult uploadFile(MultipartFile file) throws IOException {
        S3StoragePath storagePath = resolveStoragePath(file);
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

            log.info("Presigned URL 생성 완료: path={}, 유효시간={}분", s3Key, minutes);
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

            log.info("파일 삭제 및 캐시 무효화 완료: path={}", s3Key);

        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: key={}, error={}", s3Key, e.awsErrorDetails().errorMessage(), e);
            throw S3CustomException.badRequest("S3 파일 삭제 실패: " + s3Key);
        } catch (Exception e) {
            log.error("파일 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw S3CustomException.badRequest("파일 삭제 실패: " + e.getMessage());
        }
    }

    @Override
    public List<String> deleteFiles(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            log.warn("삭제할 S3 Key 목록이 비어있음");
            return List.of();
        }

        // null 또는 빈 문자열 필터링
        List<String> validKeys = s3Keys.stream()
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.toList());

        if (validKeys.isEmpty()) {
            log.warn("유효한 S3 Key가 없음");
            return List.of();
        }

        log.info("멀티 파일 삭제 시작: 전체={}, 유효={}", s3Keys.size(), validKeys.size());

        List<String> allDeletedKeys = new ArrayList<>();

        try {
            // 1. S3 멀티 삭제 (1000개씩 분할)
            allDeletedKeys = deleteMultipleFromS3(validKeys);

            // 2. CloudFront 멀티 캐시 무효화
            if (!allDeletedKeys.isEmpty()) {
                invalidateMultipleCloudFrontCache(allDeletedKeys);
            }

            log.info("멀티 파일 삭제 완료: 요청={}, 성공={}", validKeys.size(), allDeletedKeys.size());
            return allDeletedKeys;

        } catch (Exception e) {
            log.error("멀티 파일 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            // 일부 성공한 경우도 있으므로 성공한 목록 반환
            return allDeletedKeys;
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

        try {
            // S3 Key 생성 (경로 + UUID 파일명)
            String s3Key = s3KeyGenerator.generateS3Key(storagePath, file.getOriginalFilename());

            // S3 업로드 요청 생성
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .cacheControl("public, max-age=604800")
                    .build();

            // S3에 파일 업로드
            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("S3 파일 업로드 성공: bucket={}, key={}", bucketName, s3Key);

            // S3UploadResult 반환 (비즈니스 로직에서 DB 저장 처리)
            return S3UploadResult.builder()
                    .originalName(file.getOriginalFilename())
                    .path(s3Key)
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
     * S3에서 여러 파일 일괄 삭제 (1000개씩 분할 처리)
     *
     * @param s3Keys 삭제할 S3 Key 목록
     * @return 삭제 성공한 S3 Key 목록
     */
    private List<String> deleteMultipleFromS3(List<String> s3Keys) {
        List<String> allDeletedKeys = new ArrayList<>();
        int batchSize = 1000; // AWS S3 제한

        for (int i = 0; i < s3Keys.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, s3Keys.size());
            List<String> batch = s3Keys.subList(i, endIndex);

            try {
                // ObjectIdentifier 목록 생성
                List<ObjectIdentifier> objectIdentifiers = batch.stream()
                        .map(key -> ObjectIdentifier.builder().key(key).build())
                        .collect(Collectors.toList());

                // DeleteObjectsRequest 생성
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(builder -> builder.objects(objectIdentifiers).quiet(false))
                        .build();

                // S3 멀티 삭제 실행
                DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);

                // 성공한 파일 목록 수집
                List<String> deletedKeys = response.deleted().stream()
                        .map(DeletedObject::key)
                        .toList();

                allDeletedKeys.addAll(deletedKeys);

                // 실패한 파일 로깅
                if (response.hasErrors()) {
                    response.errors().forEach(error ->
                            log.error("S3 파일 삭제 실패: key={}, code={}, message={}",
                                    error.key(), error.code(), error.message())
                    );
                }

                log.info("S3 멀티 삭제 배치 완료: batch={}/{}, 성공={}, 실패={}",
                        (i / batchSize) + 1,
                        (s3Keys.size() + batchSize - 1) / batchSize,
                        deletedKeys.size(),
                        response.errors().size());

            } catch (S3Exception e) {
                log.error("S3 멀티 삭제 배치 실패: batch={}/{}, error={}",
                        (i / batchSize) + 1,
                        (s3Keys.size() + batchSize - 1) / batchSize,
                        e.awsErrorDetails().errorMessage(), e);
                // 배치 실패 시에도 계속 진행 (다음 배치 시도)
            }
        }

        return allDeletedKeys;
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
     * CloudFront 멀티 캐시 무효화
     *
     * CloudFront는 한 번의 요청으로 최대 3,000개의 경로를 무효화할 수 있습니다.
     * 와일드카드(*)를 사용하면 전체 무효화도 가능하지만, 명시적 경로 무효화가 더 안전합니다.
     *
     * @param s3Keys 캐시 무효화할 S3 Key 목록
     */
    private void invalidateMultipleCloudFrontCache(List<String> s3Keys) {
        if (distributionId == null || distributionId.isBlank()) {
            log.warn("CloudFront Distribution ID가 설정되지 않아 캐시 무효화를 건너뜁니다.");
            return;
        }

        if (s3Keys == null || s3Keys.isEmpty()) {
            log.info("캐시 무효화할 경로가 없음");
            return;
        }

        try {
            // S3 Key를 CloudFront 경로로 변환 (/로 시작)
            List<String> paths = s3Keys.stream()
                    .map(key -> "/" + key)
                    .collect(Collectors.toList());

            // CloudFront는 한 번에 최대 3,000개까지 지원하지만, 안전하게 1,000개씩 분할
            int batchSize = 1000;
            int totalBatches = (paths.size() + batchSize - 1) / batchSize;

            for (int i = 0; i < paths.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, paths.size());
                List<String> batchPaths = paths.subList(i, endIndex);

                Paths invalidationPaths = Paths.builder()
                        .items(batchPaths)
                        .quantity(batchPaths.size())
                        .build();

                InvalidationBatch batch = InvalidationBatch.builder()
                        .paths(invalidationPaths)
                        .callerReference(String.valueOf(System.currentTimeMillis()) + "-" + i)
                        .build();

                CreateInvalidationRequest request = CreateInvalidationRequest.builder()
                        .distributionId(distributionId)
                        .invalidationBatch(batch)
                        .build();

                CreateInvalidationResponse response = cloudFrontClient.createInvalidation(request);

                log.info("CloudFront 멀티 캐시 무효화 완료: batch={}/{}, invalidationId={}, paths={}",
                        (i / batchSize) + 1, totalBatches,
                        response.invalidation().id(), batchPaths.size());
            }

        } catch (Exception e) {
            log.error("CloudFront 멀티 캐시 무효화 실패: {}", e.getMessage(), e);
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
}