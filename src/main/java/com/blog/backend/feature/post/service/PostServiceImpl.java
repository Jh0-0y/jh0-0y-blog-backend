package com.blog.backend.feature.post.service;

import com.blog.backend.feature.file.entity.FileMetadata;
import com.blog.backend.feature.file.service.FileMetadataService;
import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostFile;
import com.blog.backend.feature.post.entity.PostFileType;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.repository.PostFileRepository;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.repository.PostSpecification;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
import com.blog.backend.infra.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final PostFileRepository postFileRepository;
    private final FileMetadataService fileMetadataService;
    private final S3FileService s3FileService;

    // ========== CRUD ========== //

    @Override
    @Transactional
    public PostResponse.Detail createPost(Long userId, PostRequest.Create request, MultipartFile thumbnail) {
        User user = findUserById(userId);
        existsByTitle(request.getTitle());

        // 게시글 기본 정보 저장 (썸네일 URL은 나중에 업데이트)
        Post post = Post.builder()
                .user(user)
                .postType(request.getPostType())
                .title(request.getTitle())
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.PRIVATE)
                .build();

        // 자유 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            post.updateTags(request.getTags());
        }

        // 기술 스택 처리
        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        Post savedPost = postRepository.save(post);

        // ===== 썸네일 처리 ===== //
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                log.info("게시글 생성 - 썸네일 업로드 시작: postId={}", savedPost.getId());

                // S3 업로드 (파일명: thumbnail_{postId}.jpg)
                FileMetadata thumbnailFile = s3FileService.uploadBlogThumbnail(thumbnail, savedPost.getId());

                // PostFile 매핑 생성 (fileType = THUMBNAIL)
                PostFile thumbnailMapping = PostFile.ofThumbnail(savedPost.getId(), thumbnailFile.getId());
                postFileRepository.save(thumbnailMapping);

                // Post 엔티티에 URL 저장
                savedPost.updateThumbnailUrl(thumbnailFile.getUrl());

                log.info("게시글 생성 - 썸네일 업로드 완료: postId={}, fileId={}, url={}",
                        savedPost.getId(), thumbnailFile.getId(), thumbnailFile.getUrl());

            } catch (IOException e) {
                log.error("게시글 생성 - 썸네일 업로드 실패: postId={}, error={}",
                        savedPost.getId(), e.getMessage(), e);
                throw new CustomException("썸네일 업로드에 실패했습니다.",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // ===== 본문 파일 매핑 생성 (fileType = CONTENT) ===== //
        if (request.getContentsFileIds() != null && !request.getContentsFileIds().isEmpty()) {
            log.info("게시글 생성 - 본문 파일 매핑 시작: postId={}, fileCount={}",
                    savedPost.getId(), request.getContentsFileIds().size());

            // 파일 존재 여부 검증
            fileMetadataService.validateFilesExist(request.getContentsFileIds());

            // PostFile 매핑 생성 (fileType = CONTENT)
            List<PostFile> contentFiles = request.getContentsFileIds().stream()
                    .map(fileId -> PostFile.ofContent(savedPost.getId(), fileId))
                    .collect(Collectors.toList());

            postFileRepository.saveAll(contentFiles);

            log.info("게시글 생성 - 본문 파일 매핑 완료: postId={}, mappedCount={}",
                    savedPost.getId(), contentFiles.size());
        }

        return PostResponse.Detail.from(savedPost, null, null);
    }

    @Override
    @Transactional
    public PostResponse.Detail updatePost(Long userId, Long postId, PostRequest.Update request, MultipartFile thumbnail) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        // 제목이 변경될 경우에만 중복 체크 (자기 자신 제외)
        if (!post.getTitle().equals(request.getTitle())) {
            existsByTitle(request.getTitle());
        }

        post.update(
                request.getPostType(),
                request.getTitle(),
                request.getExcerpt(),
                request.getContent(),
                request.getStatus()
        );

        // 자유 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            post.updateTags(request.getTags());
        }

        // 기술 스택 처리
        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        // ===== 썸네일 처리 ===== //
        handleThumbnailUpdate(post, thumbnail, request.getRemoveThumbnail());

        // ===== 본문 파일 매핑 처리 ===== //

        // 1. 삭제할 파일이 있으면 매핑 제거
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            log.info("게시글 수정 - 본문 파일 매핑 삭제 시작: postId={}, deleteCount={}",
                    postId, request.getDeletedFileIds().size());

            // 기존 본문 파일 매핑에서 삭제 대상만 필터링
            List<PostFile> existingContentMappings = postFileRepository
                    .findByPostIdAndFileType(postId, PostFileType.CONTENT);

            List<Long> toDelete = existingContentMappings.stream()
                    .filter(pf -> request.getDeletedFileIds().contains(pf.getFileId()))
                    .map(PostFile::getId)
                    .collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                postFileRepository.deleteAllById(toDelete);
                log.info("게시글 수정 - 본문 파일 매핑 삭제 완료: postId={}, deletedCount={}",
                        postId, toDelete.size());
            }
        }

        // 2. 새로 추가할 본문 파일이 있으면 매핑 생성
        if (request.getContentsFileIds() != null && !request.getContentsFileIds().isEmpty()) {
            log.info("게시글 수정 - 새 본문 파일 매핑 시작: postId={}, newFileCount={}",
                    postId, request.getContentsFileIds().size());

            // 파일 존재 여부 검증
            fileMetadataService.validateFilesExist(request.getContentsFileIds());

            // 중복 매핑 방지: 이미 연결된 본문 파일 제외
            List<Long> existingFileIds = postFileRepository
                    .findFileIdsByPostIdAndFileType(postId, PostFileType.CONTENT);

            List<Long> newFileIds = request.getContentsFileIds().stream()
                    .filter(fileId -> !existingFileIds.contains(fileId))
                    .collect(Collectors.toList());

            if (!newFileIds.isEmpty()) {
                List<PostFile> newMappings = newFileIds.stream()
                        .map(fileId -> PostFile.ofContent(postId, fileId))
                        .collect(Collectors.toList());

                postFileRepository.saveAll(newMappings);

                log.info("게시글 수정 - 새 본문 파일 매핑 완료: postId={}, mappedCount={}",
                        postId, newMappings.size());
            } else {
                log.info("게시글 수정 - 모든 파일이 이미 매핑되어 있음: postId={}", postId);
            }
        }

        return PostResponse.Detail.from(post, null, null);
    }

    /**
     * 썸네일 업데이트 처리 (생성/교체/삭제)
     *
     * 우선순위:
     * 1. 새 썸네일 파일이 있으면 → 기존 삭제 후 교체
     * 2. removeThumbnail = true → 기존 삭제
     * 3. 둘 다 없으면 → 유지
     */
    private void handleThumbnailUpdate(Post post, MultipartFile newThumbnail, Boolean removeThumbnail) {
        Long postId = post.getId();

        // 케이스 1: 새 썸네일 파일이 전송됨 → 교체
        if (newThumbnail != null && !newThumbnail.isEmpty()) {
            log.info("게시글 수정 - 썸네일 교체 시작: postId={}", postId);

            // 기존 썸네일 삭제
            deleteExistingThumbnail(postId);

            // 새 썸네일 업로드
            try {
                FileMetadata thumbnailFile = s3FileService.uploadBlogThumbnail(newThumbnail, postId);

                // PostFile 매핑 생성
                PostFile thumbnailMapping = PostFile.ofThumbnail(postId, thumbnailFile.getId());
                postFileRepository.save(thumbnailMapping);

                // Post 엔티티에 URL 업데이트
                post.updateThumbnailUrl(thumbnailFile.getUrl());

                log.info("게시글 수정 - 썸네일 교체 완료: postId={}, fileId={}, url={}",
                        postId, thumbnailFile.getId(), thumbnailFile.getUrl());

            } catch (IOException e) {
                log.error("게시글 수정 - 썸네일 업로드 실패: postId={}, error={}",
                        postId, e.getMessage(), e);
                throw new CustomException("썸네일 업로드에 실패했습니다.",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return;
        }

        // 케이스 2: removeThumbnail = true → 삭제
        if (Boolean.TRUE.equals(removeThumbnail)) {
            log.info("게시글 수정 - 썸네일 제거 시작: postId={}", postId);
            deleteExistingThumbnail(postId);
            post.removeThumbnail();
            log.info("게시글 수정 - 썸네일 제거 완료: postId={}", postId);
            return;
        }

        // 케이스 3: 유지 (아무 작업 안 함)
        log.info("게시글 수정 - 썸네일 유지: postId={}", postId);
    }

    /**
     * 기존 썸네일 삭제 (S3 + DB + PostFile 매핑)
     */
    private void deleteExistingThumbnail(Long postId) {
        Optional<PostFile> existingThumbnail = postFileRepository
                .findTopByPostIdAndFileType(postId, PostFileType.THUMBNAIL);

        if (existingThumbnail.isPresent()) {
            Long fileId = existingThumbnail.get().getFileId();

            try {
                // FileMetadata 조회
                FileMetadata oldFile = fileMetadataService.getFileMetadata(fileId);

                // S3 + DB 삭제
                s3FileService.deleteFile(oldFile);

                // PostFile 매핑 삭제는 이미 deleteFile에서 처리됨
                log.info("기존 썸네일 삭제 완료: postId={}, fileId={}", postId, fileId);

            } catch (Exception e) {
                log.error("기존 썸네일 삭제 실패: postId={}, fileId={}, error={}",
                        postId, fileId, e.getMessage(), e);
                // 삭제 실패해도 계속 진행 (새 썸네일 업로드는 성공해야 함)
            }
        }
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        // ===== 썸네일 삭제 ===== //
        if (post.hasThumbnail()) {
            log.info("게시글 삭제 - 썸네일 삭제 시작: postId={}", postId);
            deleteExistingThumbnail(postId);
        }

        // ===== 파일 매핑 삭제 (벌크 연산) ===== //
        log.info("게시글 삭제 - 파일 매핑 삭제 시작: postId={}", postId);

        int deletedMappings = postFileRepository.deleteByPostId(postId);

        log.info("게시글 삭제 - 파일 매핑 삭제 완료: postId={}, deletedMappings={}", postId, deletedMappings);

        // 주의: 본문 파일(FileMetadata)은 삭제하지 않음
        // 고아 파일이 된 본문 파일들은 스케줄러가 24시간 후 자동 삭제

        post.clearStack();
        postRepository.delete(post);

        log.info("게시글 삭제 완료: postId={}", postId);
    }

    @Override
    public PostResponse.Detail getPost(Long postId) {
        Post post = postRepository.findByIdWithStacks(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        Post prevPost = postRepository.findPreviousPost(postId, PostStatus.PUBLIC).orElse(null);
        Post nextPost = postRepository.findNextPost(postId, PostStatus.PUBLIC).orElse(null);

        return PostResponse.Detail.from(post, prevPost, nextPost);
    }

    // ========== 복합 필터링 (신규) ========== //

    @Override
    public Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withCondition(condition), pageable)
                .map(PostResponse.PostItems::from);
    }

    @Override
    public Page<PostResponse.PostItems> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withUserAndCondition(userId, condition), pageable)
                .map(PostResponse.PostItems::from);
    }

    // ========== 기존 메서드 (하위 호환) ========== //

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getPublicPosts(Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getPostsByPostType(PostType postType, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(postType, null, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getPostsByTag(String tagName, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, tagName, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> searchPosts(String keyword, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, keyword);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getMyPosts(Long userId, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofMine(null, null, null);
        return searchMyPosts(userId, condition, pageable);
    }

    // ========== Private Methods ========== //

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    private void existsByTitle(String title) {
        if (postRepository.existsByTitle(title)) {
            throw CustomException.conflict("이미 존재하는 제목입니다.");
        }
    }

    private void validateAuthor(Post post, Long userId) {
        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("게시글 수정/삭제 권한이 없습니다");
        }
    }
}