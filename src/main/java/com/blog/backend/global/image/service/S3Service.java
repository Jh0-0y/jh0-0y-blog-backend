//package com.blog.backend.global.image.service;
//
//import com.blog.backend.global.error.CustomException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class S3Service {
//
//    private final S3Client s3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    @Value("${cloud.aws.region.static}")
//    private String region;
//
//    @Value("${image.upload.path-prefix}")
//    private String pathPrefix;
//
//    @Value("${image.upload.max-size}")
//    private long maxFileSize;
//
//    @Value("${image.upload.allowed-extensions}")
//    private String allowedExtensions;
//
//    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
//            "image/jpeg", "image/png", "image/gif", "image/webp"
//    );
//
//    /**
//     * 파일 업로드
//     */
//    public UploadResult upload(MultipartFile file) {
//        validateFile(file);
//
//        String originalName = file.getOriginalFilename();
//        String storedName = generateStoredName(originalName);
//        String key = pathPrefix + "/" + storedName;
//
//        try {
//            PutObjectRequest request = PutObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(key)
//                    .contentType(file.getContentType())
//                    .contentLength(file.getSize())
//                    .build();
//
//            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//
//            String url = getUrl(key);
//
//            log.info("S3 업로드 성공: {}", url);
//
//            return new UploadResult(
//                    originalName,
//                    storedName,
//                    url,
//                    file.getContentType(),
//                    file.getSize()
//            );
//
//        } catch (IOException e) {
//            log.error("S3 업로드 실패: {}", e.getMessage());
//            throw CustomException.badRequest("파일 업로드에 실패했습니다.");
//        }
//    }
//
//    /**
//     * 파일 삭제
//     */
//    public void delete(String url) {
//        try {
//            String key = extractKeyFromUrl(url);
//
//            DeleteObjectRequest request = DeleteObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(key)
//                    .build();
//
//            s3Client.deleteObject(request);
//            log.info("S3 삭제 성공: {}", url);
//        } catch (Exception e) {
//            log.error("S3 삭제 실패: {}", e.getMessage());
//            // 삭제 실패는 크리티컬하지 않으므로 예외를 던지지 않음
//        }
//    }
//
//    /**
//     * 여러 파일 삭제
//     */
//    public void deleteAll(List<String> urls) {
//        urls.forEach(this::delete);
//    }
//
//    /**
//     * 파일 유효성 검사
//     */
//    private void validateFile(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            throw CustomException.badRequest("파일이 비어있습니다.");
//        }
//
//        if (file.getSize() > maxFileSize) {
//            throw CustomException.badRequest("파일 크기가 제한을 초과했습니다.");
//        }
//
//        String contentType = file.getContentType();
//        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
//            throw CustomException.badRequest("허용되지 않는 파일 형식입니다.");
//        }
//
//        String extension = getExtension(file.getOriginalFilename());
//        List<String> allowedList = Arrays.asList(allowedExtensions.split(","));
//        if (!allowedList.contains(extension.toLowerCase())) {
//            throw CustomException.badRequest("허용되지 않는 파일 형식입니다.");
//        }
//    }
//
//    /**
//     * 저장용 파일명 생성 (UUID + 확장자)
//     */
//    private String generateStoredName(String originalName) {
//        String extension = getExtension(originalName);
//        return UUID.randomUUID().toString() + "." + extension;
//    }
//
//    /**
//     * 확장자 추출
//     */
//    private String getExtension(String filename) {
//        if (filename == null || !filename.contains(".")) {
//            return "";
//        }
//        return filename.substring(filename.lastIndexOf(".") + 1);
//    }
//
//    /**
//     * S3 URL 생성
//     */
//    private String getUrl(String key) {
//        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
//    }
//
//    /**
//     * URL에서 S3 키 추출
//     */
//    private String extractKeyFromUrl(String url) {
//        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
//        return url.replace(baseUrl, "");
//    }
//
//    /**
//     * 업로드 결과 DTO
//     */
//    public record UploadResult(
//            String originalName,
//            String storedName,
//            String url,
//            String contentType,
//            Long fileSize
//    ) {}
//}