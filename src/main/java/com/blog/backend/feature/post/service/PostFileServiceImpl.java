package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.entity.PostFile;
import com.blog.backend.feature.post.entity.PostFileType;
import com.blog.backend.feature.post.repository.PostFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PostFile 중간 테이블 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFileServiceImpl implements PostFileService {

    private final PostFileRepository postFileRepository;

    @Override
    @Transactional
    public void saveThumbnailMapping(Long postId, Long fileId) {
        PostFile thumbnailMapping = PostFile.ofThumbnail(postId, fileId);
        postFileRepository.save(thumbnailMapping);
        log.info("썸네일 매핑 저장 완료: postId={}, fileId={}", postId, fileId);
    }

    @Override
    @Transactional
    public void saveContentFileMappings(Long postId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.info("저장할 본문 파일이 없음: postId={}", postId);
            return;
        }

        List<PostFile> contentMappings = fileIds.stream()
                .map(fileId -> PostFile.ofContent(postId, fileId))
                .collect(Collectors.toList());

        postFileRepository.saveAll(contentMappings);
        log.info("본문 파일 매핑 저장 완료: postId={}, count={}", postId, contentMappings.size());
    }

    @Override
    @Transactional
    public void deleteExistingThumbnail(Long postId) {
        Optional<PostFile> existingThumbnail = postFileRepository
                .findTopByPostIdAndFileType(postId, PostFileType.THUMBNAIL);

        if (existingThumbnail.isPresent()) {
            postFileRepository.delete(existingThumbnail.get());
            log.info("기존 썸네일 매핑 삭제 완료: postId={}, fileId={}",
                    postId, existingThumbnail.get().getFileId());
        } else {
            log.info("삭제할 썸네일 매핑이 없음: postId={}", postId);
        }
    }

    @Override
    @Transactional
    public void deleteContentFileMappings(Long postId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.info("삭제할 본문 파일이 없음: postId={}", postId);
            return;
        }

        List<PostFile> existingMappings = postFileRepository
                .findByPostIdAndFileType(postId, PostFileType.CONTENT);

        List<Long> toDeleteIds = existingMappings.stream()
                .filter(pf -> fileIds.contains(pf.getFileId()))
                .map(PostFile::getId)
                .collect(Collectors.toList());

        if (!toDeleteIds.isEmpty()) {
            postFileRepository.deleteAllById(toDeleteIds);
            log.info("본문 파일 매핑 삭제 완료: postId={}, deletedCount={}", postId, toDeleteIds.size());
        } else {
            log.info("삭제할 본문 파일 매핑이 없음: postId={}", postId);
        }
    }

    @Override
    @Transactional
    public int deleteAllMappingsByPostId(Long postId) {
        int deletedCount = postFileRepository.deleteByPostId(postId);
        log.info("게시글의 모든 파일 매핑 삭제 완료: postId={}, deletedCount={}", postId, deletedCount);
        return deletedCount;
    }

    @Override
    public Set<Long> getContentFileIds(Long postId) {
        Set<Long> fileIds = postFileRepository
                .findFileIdsByPostIdAndFileType(postId, PostFileType.CONTENT);
        log.info("본문 파일 ID 조회 완료: postId={}, count={}", postId, fileIds.size());
        return fileIds;
    }

    @Override
    public Long getThumbnailFileId(Long postId) {
        Optional<PostFile> thumbnail = postFileRepository
                .findTopByPostIdAndFileType(postId, PostFileType.THUMBNAIL);

        if (thumbnail.isPresent()) {
            Long fileId = thumbnail.get().getFileId();
            log.info("썸네일 파일 ID 조회 완료: postId={}, fileId={}", postId, fileId);
            return fileId;
        }

        log.info("썸네일 파일이 없음: postId={}", postId);
        return null;
    }

    @Override
    public Set<Long> collectUsedFileIds() {
        List<Long> usedFileIds = postFileRepository.findAllUsedFileIds();
        Set<Long> uniqueFileIds = Set.copyOf(usedFileIds);
        log.info("PostFile에서 사용 중인 파일 ID 수집 완료: count={}", uniqueFileIds.size());
        return uniqueFileIds;
    }
}