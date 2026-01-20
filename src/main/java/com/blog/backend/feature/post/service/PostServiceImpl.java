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
import com.blog.backend.feature.post.utils.SlugGenerator;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
import com.blog.backend.infra.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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

        // Slug 생성 (중복 처리 포함)
        String slug = generateUniqueSlug(request.getTitle());

        // 게시글 기본 정보 저장
        Post post = Post.builder()
                .user(user)
                .postType(request.getPostType())
                .title(request.getTitle())
                .slug(slug)
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
        log.info("게시글 생성 완료: postId={}, slug={}", savedPost.getId(), savedPost.getSlug());

        // 썸네일 처리
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                log.info("게시글 생성 - 썸네일 업로드 시작: postId={}", savedPost.getId());

                FileMetadata thumbnailFile = s3FileService.uploadBlogThumbnail(thumbnail, savedPost.getId());

                PostFile thumbnailMapping = PostFile.ofThumbnail(savedPost.getId(), thumbnailFile.getId());
                postFileRepository.save(thumbnailMapping);

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

        // 본문 파일 매핑 생성
        if (request.getContentsFileIds() != null && !request.getContentsFileIds().isEmpty()) {
            log.info("게시글 생성 - 본문 파일 매핑 시작: postId={}, fileCount={}",
                    savedPost.getId(), request.getContentsFileIds().size());

            fileMetadataService.validateFilesExist(request.getContentsFileIds());

            List<PostFile> contentFiles = request.getContentsFileIds().stream()
                    .map(fileId -> PostFile.ofContent(savedPost.getId(), fileId))
                    .collect(Collectors.toList());

            postFileRepository.saveAll(contentFiles);

            log.info("게시글 생성 - 본문 파일 매핑 완료: postId={}, mappedCount={}",
                    savedPost.getId(), contentFiles.size());
        }

        // 트랜잭션 내에서 모든 데이터 준비
        return buildPostDetailResponse(savedPost);
    }

    @Override
    @Transactional
    public PostResponse.Detail updatePostBySlug(Long userId, String slug, PostRequest.Update request, MultipartFile thumbnail) {
        Post post = findPostBySlug(slug);
        validateAuthor(post, userId);

        // 제목이 변경될 경우에만 중복 체크 및 slug 재생성
        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            existsByTitle(request.getTitle());
            newSlug = generateUniqueSlug(request.getTitle());
            log.info("게시글 수정 - 제목 변경으로 slug 재생성: postId={}, oldSlug={}, newSlug={}",
                    post.getId(), post.getSlug(), newSlug);
        }

        post.update(
                request.getPostType(),
                request.getTitle(),
                newSlug,
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

        // 썸네일 처리
        handleThumbnailUpdate(post, thumbnail, request.getRemoveThumbnail());

        // 본문 파일 매핑 처리
        handleContentFilesUpdate(post, request);

        // 트랜잭션 내에서 모든 데이터 준비
        return buildPostDetailResponse(post);
    }

    @Override
    @Transactional
    public void deletePostBySlug(Long userId, String slug) {
        Post post = findPostBySlug(slug);
        validateAuthor(post, userId);

        // 썸네일 삭제
        if (post.hasThumbnail()) {
            log.info("게시글 삭제 - 썸네일 삭제 시작: postId={}", post.getId());
            deleteExistingThumbnail(post.getId());
        }

        // 파일 매핑 삭제
        log.info("게시글 삭제 - 파일 매핑 삭제 시작: postId={}", post.getId());
        int deletedMappings = postFileRepository.deleteByPostId(post.getId());
        log.info("게시글 삭제 - 파일 매핑 삭제 완료: postId={}, deletedMappings={}", post.getId(), deletedMappings);

        post.clearStack();
        postRepository.delete(post);

        log.info("게시글 삭제 완료: postId={}, slug={}", post.getId(), post.getSlug());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse.Detail getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithStacks(slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        // 트랜잭션 내에서 모든 데이터 준비
        return buildPostDetailResponse(post);
    }

    // ========== 복합 필터링 ========== //

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withCondition(condition), pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse.PostItems> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withUserAndCondition(userId, condition), pageable)
                .map(this::buildPostItemsResponse);
    }

    // ========== DTO 빌더 메서드 (트랜잭션 내에서 실행) ========== //

    /**
     * Post 엔티티로부터 PostItems DTO 생성
     * 트랜잭션 내에서 모든 lazy loading 처리
     */
    private PostResponse.PostItems buildPostItemsResponse(Post post) {
        // 트랜잭션 내에서 Stack 이름 추출
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        // 트랜잭션 내에서 tags 초기화 (방어적 복사)
        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        return PostResponse.PostItems.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPostType(),
                post.getStatus(),
                post.getThumbnailUrl(),
                tags,
                stackNames,
                post.getCreatedAt()
        );
    }

    /**
     * Post 엔티티로부터 Detail DTO 생성 (관련 게시글 포함)
     * 트랜잭션 내에서 모든 lazy loading 처리
     */
    private PostResponse.Detail buildPostDetailResponse(Post post) {
        // 트랜잭션 내에서 Stack 이름 추출
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        // 트랜잭션 내에서 tags 초기화 (방어적 복사)
        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        // 관련 게시글 조회 및 DTO 변환
        List<Post> relatedPosts = getRelatedPosts(post);
        List<PostResponse.PostItems> relatedPostItems = relatedPosts.stream()
                .map(this::buildPostItemsResponse)
                .collect(Collectors.toList());

        return PostResponse.Detail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPostType(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailUrl(),
                tags,
                stackNames,
                relatedPostItems,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== 관련 게시글 추천 로직 ========== //

    /**
     * 관련 게시글 3개 조회
     *
     * 우선순위:
     * 1순위: Stack 교집합 많음 + PostType 일치 + 최신순 (1~2개)
     * 2순위: Stack 교집합 많음 + PostType 다름 + 최신순 (나머지)
     * 3순위: 최신 공개 게시글 (Stack이 없거나 부족할 때)
     */
    private List<Post> getRelatedPosts(Post currentPost) {
        List<Post> relatedPosts = new ArrayList<>();

        // 현재 게시글의 Stack 목록 (트랜잭션 내에서 추출)
        List<String> stackNames = currentPost.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        // Stack이 없으면 최신 공개 게시글 3개 반환
        if (stackNames.isEmpty()) {
            log.info("관련 게시글 조회 - Stack 없음, 최신 공개 게시글 조회: postId={}", currentPost.getId());
            return postRepository.findLatestPublicPosts(
                    currentPost.getId(),
                    PostStatus.PUBLIC,
                    PageRequest.of(0, 3)
            );
        }

        // 1순위: Stack 일치 + PostType 일치 (최대 2개)
        List<Post> firstPriority = postRepository.findRelatedPostsByStackAndType(
                currentPost.getId(),
                stackNames,
                currentPost.getPostType(),
                PostStatus.PUBLIC,
                PageRequest.of(0, 2)
        );

        relatedPosts.addAll(firstPriority);
        log.info("관련 게시글 조회 - 1순위: postId={}, count={}", currentPost.getId(), firstPriority.size());

        // 필요한 개수 계산
        int remaining = 3 - relatedPosts.size();

        // 2순위: Stack 일치 + PostType 다름 (나머지 개수만큼)
        if (remaining > 0) {
            List<Post> secondPriority = postRepository.findRelatedPostsByStackOnly(
                    currentPost.getId(),
                    stackNames,
                    currentPost.getPostType(),
                    PostStatus.PUBLIC,
                    PageRequest.of(0, remaining)
            );

            relatedPosts.addAll(secondPriority);
            log.info("관련 게시글 조회 - 2순위: postId={}, count={}", currentPost.getId(), secondPriority.size());

            remaining = 3 - relatedPosts.size();
        }

        // 3순위: 여전히 부족하면 최신 공개 게시글로 채움
        if (remaining > 0) {
            List<Post> latestPosts = postRepository.findLatestPublicPosts(
                    currentPost.getId(),
                    PostStatus.PUBLIC,
                    PageRequest.of(0, remaining)
            );

            // 중복 제거 (이미 추가된 게시글 제외)
            List<Long> existingIds = relatedPosts.stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());

            List<Post> filtered = latestPosts.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .limit(remaining)
                    .collect(Collectors.toList());

            relatedPosts.addAll(filtered);
            log.info("관련 게시글 조회 - 3순위: postId={}, count={}", currentPost.getId(), filtered.size());
        }

        log.info("관련 게시글 조회 완료: postId={}, totalCount={}", currentPost.getId(), relatedPosts.size());
        return relatedPosts;
    }

    // ========== 본문 파일 매핑 처리 ========== //

    /**
     * 본문 파일 매핑 업데이트 처리
     */
    private void handleContentFilesUpdate(Post post, PostRequest.Update request) {
        // 1. 삭제할 파일이 있으면 매핑 제거
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            log.info("게시글 수정 - 본문 파일 매핑 삭제 시작: postId={}, deleteCount={}",
                    post.getId(), request.getDeletedFileIds().size());

            List<PostFile> existingContentMappings = postFileRepository
                    .findByPostIdAndFileType(post.getId(), PostFileType.CONTENT);

            List<Long> toDelete = existingContentMappings.stream()
                    .filter(pf -> request.getDeletedFileIds().contains(pf.getFileId()))
                    .map(PostFile::getId)
                    .collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                postFileRepository.deleteAllById(toDelete);
                log.info("게시글 수정 - 본문 파일 매핑 삭제 완료: postId={}, deletedCount={}",
                        post.getId(), toDelete.size());
            }
        }

        // 2. 새로 추가할 본문 파일이 있으면 매핑 생성
        if (request.getContentsFileIds() != null && !request.getContentsFileIds().isEmpty()) {
            log.info("게시글 수정 - 새 본문 파일 매핑 시작: postId={}, newFileCount={}",
                    post.getId(), request.getContentsFileIds().size());

            fileMetadataService.validateFilesExist(request.getContentsFileIds());

            List<Long> existingFileIds = postFileRepository
                    .findFileIdsByPostIdAndFileType(post.getId(), PostFileType.CONTENT);

            List<Long> newFileIds = request.getContentsFileIds().stream()
                    .filter(fileId -> !existingFileIds.contains(fileId))
                    .collect(Collectors.toList());

            if (!newFileIds.isEmpty()) {
                List<PostFile> newMappings = newFileIds.stream()
                        .map(fileId -> PostFile.ofContent(post.getId(), fileId))
                        .collect(Collectors.toList());

                postFileRepository.saveAll(newMappings);

                log.info("게시글 수정 - 새 본문 파일 매핑 완료: postId={}, newMappedCount={}",
                        post.getId(), newMappings.size());
            }
        }
    }

    // ========== 썸네일 처리 ========== //

    /**
     * 썸네일 업데이트 처리
     *
     * 케이스 1: 새 썸네일 파일 전송 → 기존 삭제 + 새로 업로드
     * 케이스 2: removeThumbnail = true → 삭제
     * 케이스 3: 둘 다 없음 → 유지
     */
    private void handleThumbnailUpdate(Post post, MultipartFile thumbnail, Boolean removeThumbnail) {
        Long postId = post.getId();

        // 케이스 1: 새 썸네일 파일 전송 → 교체
        if (thumbnail != null && !thumbnail.isEmpty()) {
            log.info("게시글 수정 - 썸네일 교체 시작: postId={}", postId);

            // 기존 썸네일 삭제
            deleteExistingThumbnail(postId);

            // 새 썸네일 업로드
            try {
                FileMetadata thumbnailFile = s3FileService.uploadBlogThumbnail(thumbnail, postId);

                PostFile thumbnailMapping = PostFile.ofThumbnail(postId, thumbnailFile.getId());
                postFileRepository.save(thumbnailMapping);

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

        // 케이스 3: 유지
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
                FileMetadata oldFile = fileMetadataService.getFileMetadata(fileId);
                s3FileService.deleteFile(oldFile);
                log.info("기존 썸네일 삭제 완료: postId={}, fileId={}", postId, fileId);

            } catch (Exception e) {
                log.error("기존 썸네일 삭제 실패: postId={}, fileId={}, error={}",
                        postId, fileId, e.getMessage(), e);
            }
        }
    }

    // ========== Slug 생성 로직 ========== //

    /**
     * 중복되지 않는 고유한 slug 생성
     */
    private String generateUniqueSlug(String title) {
        String baseSlug = SlugGenerator.generate(title);

        // 중복 체크
        if (!postRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        // 중복이면 번호 추가
        long count = postRepository.countBySlugStartingWith(baseSlug + "%");

        // 번호를 붙여서 재시도 (최대 100번)
        for (int i = 2; i <= 100; i++) {
            String candidateSlug = SlugGenerator.generateWithSuffix(baseSlug, i);
            if (!postRepository.existsBySlug(candidateSlug)) {
                log.info("Slug 중복으로 번호 추가: baseSlug={}, finalSlug={}", baseSlug, candidateSlug);
                return candidateSlug;
            }
        }

        // 100번 시도해도 실패하면 타임스탬프 추가
        String timestampSlug = baseSlug + "-" + System.currentTimeMillis();
        log.warn("Slug 중복 해소 실패, 타임스탬프 추가: {}", timestampSlug);
        return timestampSlug;
    }

    // ========== 기존 메서드 (하위 호환, Deprecated) ========== //

    @Override
    @Deprecated
    public PostResponse.Detail updatePost(Long userId, Long postId, PostRequest.Update request, MultipartFile thumbnail) {
        Post post = findPostById(postId);
        return updatePostBySlug(userId, post.getSlug(), request, thumbnail);
    }

    @Override
    @Deprecated
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);
        deletePostBySlug(userId, post.getSlug());
    }

    @Override
    @Deprecated
    public PostResponse.Detail getPost(Long postId) {
        Post post = postRepository.findByIdWithStacks(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        return buildPostDetailResponse(post);
    }

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

    // ========== Private Helper Methods ========== //

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    private Post findPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
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