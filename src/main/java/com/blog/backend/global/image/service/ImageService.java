package com.blog.backend.global.image.service;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.global.image.dto.ImageResponse;
import com.blog.backend.global.image.entity.Image;
import com.blog.backend.global.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    // 마크다운/HTML에서 이미지 URL 추출용 정규식
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
            "!\\[.*?\\]\\((https?://[^)]+)\\)|<img[^>]+src=[\"'](https?://[^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 이미지 업로드
     */
    @Transactional
    public ImageResponse upload(MultipartFile file) {
        S3Service.UploadResult result = s3Service.upload(file);

        Image image = Image.builder()
                .originalName(result.originalName())
                .storedName(result.storedName())
                .url(result.url())
                .contentType(result.contentType())
                .fileSize(result.fileSize())
                .build();

        Image savedImage = imageRepository.save(image);

        log.info("이미지 업로드 완료: id={}, url={}", savedImage.getId(), savedImage.getUrl());

        return ImageResponse.from(savedImage);
    }

    /**
     * 게시글 저장/수정 시 이미지 연결 처리
     * - 본문에서 사용 중인 이미지 URL 추출
     * - 해당 이미지들을 게시글에 연결
     * - 기존에 연결되어 있었지만 이제 사용하지 않는 이미지는 삭제
     */
    @Transactional
    public void syncImagesWithPost(Post post, String content) {
        List<String> usedUrls = extractImageUrls(content);

        if (post.getId() != null) {
            // 기존 게시글 수정 시: 사용하지 않는 이미지 정리
            List<Image> existingImages = imageRepository.findByPostId(post.getId());
            List<Image> unusedImages = existingImages.stream()
                    .filter(img -> !usedUrls.contains(img.getUrl()))
                    .toList();

            // 사용하지 않는 이미지 S3에서 삭제
            for (Image unusedImage : unusedImages) {
                s3Service.delete(unusedImage.getUrl());
                imageRepository.delete(unusedImage);
                log.info("미사용 이미지 삭제: {}", unusedImage.getUrl());
            }
        }

        // 사용 중인 이미지 게시글에 연결
        if (!usedUrls.isEmpty()) {
            List<Image> usedImages = imageRepository.findByUrlIn(usedUrls);
            for (Image image : usedImages) {
                image.attachToPost(post);
            }
            log.info("게시글 {}에 이미지 {}개 연결", post.getId(), usedImages.size());
        }
    }

    /**
     * 게시글 삭제 시 연결된 이미지 모두 삭제
     */
    @Transactional
    public void deleteAllByPost(Post post) {
        List<Image> images = imageRepository.findByPost(post);

        for (Image image : images) {
            s3Service.delete(image.getUrl());
            imageRepository.delete(image);
        }

        log.info("게시글 {} 이미지 {}개 삭제", post.getId(), images.size());
    }

    /**
     * 고아 이미지 정리 (스케줄러에서 호출)
     * - 임시 상태로 24시간 이상 지난 이미지 삭제
     */
    @Transactional
    public int cleanupOrphanImages() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Image> orphanImages = imageRepository.findOrphanImages(threshold);

        for (Image image : orphanImages) {
            s3Service.delete(image.getUrl());
            imageRepository.delete(image);
        }

        log.info("고아 이미지 {}개 정리 완료", orphanImages.size());
        return orphanImages.size();
    }

    /**
     * 본문에서 이미지 URL 추출
     */
    public List<String> extractImageUrls(String content) {
        List<String> urls = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return urls;
        }

        Matcher matcher = IMAGE_URL_PATTERN.matcher(content);
        while (matcher.find()) {
            String url = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (url != null) {
                urls.add(url);
            }
        }

        return urls;
    }
}