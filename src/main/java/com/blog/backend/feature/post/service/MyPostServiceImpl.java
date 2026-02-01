package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.repository.PostSpecification;
import com.blog.backend.feature.post.util.MarkdownFileParser;
import com.blog.backend.feature.post.util.SlugGenerator;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.user.entity.User;
import com.blog.backend.feature.user.repository.UserRepository;
import com.blog.backend.global.core.exception.CustomException;
import com.blog.backend.global.file.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPostServiceImpl implements MyPostService{

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final PostFileService postFileService;
    private final FileMetadataService fileMetadataService;

    // ========== CRUD ========== //

    @Override
    @Transactional
    public PostResponse.Detail createPost(User user, PostRequest.Create request) {
        validateTitleForCreate(request.getTitle());

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
                .status(PostStatus.PUBLISHED)
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

        // 썸네일 처리 (사전 업로드된 파일)
        if (request.getThumbnailFileId() != null) {
            handleThumbnailFromPreUpload(savedPost, request.getThumbnailFileId(), request.getThumbnailPath());
        }

        // 본문 파일 매핑 생성 (본문에서 파싱)
        handleContentFilesFromMarkdown(savedPost.getId(), request.getContent());

        return buildPostDetailResponse(savedPost);
    }

    @Override
    public PostResponse.Edit getPostForEdit(Long userId, String slug) {
        // 본인 게시글은 상태 무관하게 조회
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        return PostResponse.Edit.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPostType(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailPath(),
                tags,
                stackNames,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public PostResponse.Detail updatePost(Long userId, String slug, PostRequest.Update request) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        // 제목이 변경될 경우에만 중복 체크 및 slug 재생성
        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            validateTitleForUpdate(post.getId(), request.getTitle());
            newSlug = generateUniqueSlug(request.getTitle());
            log.info("게시글 수정 - 제목 변경으로 slug 재생성: postId={}, oldSlug={}, newSlug={}",
                    post.getId(), post.getSlug(), newSlug);
        }

        post.update(
                request.getPostType(),
                request.getTitle(),
                newSlug,
                request.getExcerpt(),
                request.getContent()
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
        handleThumbnailUpdate(post, request);

        // 본문 파일 매핑 처리 (차집합 기반)
        handleContentFilesUpdate(post.getId(), request.getContent());

        return buildPostDetailResponse(post);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, String slug) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        // 소프트 삭제 처리
        post.softDelete();

        log.info("게시글 소프트 삭제 완료: postId={}, slug={}, deletedAt={}",
                post.getId(), post.getSlug(), post.getDeletedAt());
    }

    @Override
    @Transactional
    public void restorePost(Long userId, String slug) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        // 삭제된 상태가 아니면 복구 불가
        if (post.getStatus() != PostStatus.DELETED) {
            throw CustomException.badRequest("삭제되지 않은 게시글은 복구할 수 없습니다");
        }

        // 복구 처리
        post.restoreFromDelete();

        log.info("게시글 복구 완료: postId={}, slug={}", post.getId(), post.getSlug());
    }

    // ========== 조회 ========== //

    @Override
    public Page<PostResponse.PostItems> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable) {

        PostSearchCondition nonDeletedCondition = PostSearchCondition.builder()
                .postType(condition.getPostType())
                .stackName(condition.getStackName())
                .keyword(condition.getKeyword())
                .build();

        return postRepository.findAll(
                PostSpecification.withUserAndCondition(userId, nonDeletedCondition),
                pageable
        ).map(this::buildPostItemsResponse);
    }

    @Override
    public Page<PostResponse.PostItems> getDeletedPosts(Long userId, Pageable pageable) {
        return postRepository.findDeletedPostsByUserId(userId, pageable)
                .map(this::buildPostItemsResponse);
    }

    // ========== DTO 빌더 메서드 ========== //

    private PostResponse.PostItems buildPostItemsResponse(Post post) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        // 작성자 정보 생성
        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                post.getUser().getProfileImagePath()
        );

        return PostResponse.PostItems.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPostType(),
                post.getStatus(),
                post.getThumbnailPath(),
                tags,
                stackNames,
                author,
                post.getCreatedAt()
        );
    }

    private PostResponse.Detail buildPostDetailResponse(Post post) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        // 작성자 정보 생성
        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                post.getUser().getProfileImagePath()
        );

        return PostResponse.Detail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPostType(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailPath(),
                tags,
                stackNames,
                author,
                List.of(), // 내 게시글 조회 시에는 관련 게시글 불필요
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== 파일 처리 ========== //

    private void handleContentFilesFromMarkdown(Long postId, String content) {
        Set<Long> fileIds = MarkdownFileParser.extractFileIds(content);

        if (fileIds.isEmpty()) {
            log.info("게시글 생성 - 본문에 파일 참조 없음: postId={}", postId);
            return;
        }

        log.info("게시글 생성 - 본문 파일 매핑 시작: postId={}, fileCount={}", postId, fileIds.size());

        fileMetadataService.validateFilesExist(new ArrayList<>(fileIds));
        postFileService.saveContentFileMappings(postId, new ArrayList<>(fileIds));

        log.info("게시글 생성 - 본문 파일 매핑 완료: postId={}", postId);
    }

    private void handleContentFilesUpdate(Long postId, String newContent) {
        Set<Long> oldFileIds = postFileService.getContentFileIds(postId);
        Set<Long> newFileIds = MarkdownFileParser.extractFileIds(newContent);

        Set<Long> fileIdsToDelete = new HashSet<>(oldFileIds);
        fileIdsToDelete.removeAll(newFileIds);

        Set<Long> fileIdsToAdd = new HashSet<>(newFileIds);
        fileIdsToAdd.removeAll(oldFileIds);

        log.info("게시글 수정 - 파일 매핑 변경 분석: postId={}, 기존={}, 신규={}, 삭제={}, 추가={}",
                postId, oldFileIds.size(), newFileIds.size(),
                fileIdsToDelete.size(), fileIdsToAdd.size());

        if (!fileIdsToDelete.isEmpty()) {
            postFileService.deleteContentFileMappings(postId, new ArrayList<>(fileIdsToDelete));
        }

        if (!fileIdsToAdd.isEmpty()) {
            fileMetadataService.validateFilesExist(new ArrayList<>(fileIdsToAdd));
            postFileService.saveContentFileMappings(postId, new ArrayList<>(fileIdsToAdd));
        }
    }

    private void handleThumbnailFromPreUpload(Post post, Long thumbnailFileId, String thumbnailUrl) {
        log.info("게시글 생성 - 썸네일 처리 시작: postId={}, fileId={}", post.getId(), thumbnailFileId);

        fileMetadataService.validateFilesExist(List.of(thumbnailFileId));
        postFileService.saveThumbnailMapping(post.getId(), thumbnailFileId);
        post.updateThumbnailUrl(thumbnailUrl);

        log.info("게시글 생성 - 썸네일 처리 완료: postId={}, path={}", post.getId(), thumbnailUrl);
    }

    private void handleThumbnailUpdate(Post post, PostRequest.Update request) {
        if (request.getThumbnailFileId() != null) {
            fileMetadataService.validateFilesExist(List.of(request.getThumbnailFileId()));
            postFileService.deleteExistingThumbnail(post.getId());
            postFileService.saveThumbnailMapping(post.getId(), request.getThumbnailFileId());
            post.updateThumbnailUrl(request.getThumbnailPath());
            return;
        }

        if (Boolean.TRUE.equals(request.getRemoveThumbnail())) {
            postFileService.deleteExistingThumbnail(post.getId());
            post.removeThumbnail();
        }
    }

    // ========== Slug 생성 로직 ========== //

    private String generateUniqueSlug(String title) {
        String baseSlug = SlugGenerator.generate(title);

        if (!postRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        for (int i = 2; i <= 100; i++) {
            String candidateSlug = SlugGenerator.generateWithSuffix(baseSlug, i);
            if (!postRepository.existsBySlug(candidateSlug)) {
                log.info("Slug 중복으로 번호 추가: baseSlug={}, finalSlug={}", baseSlug, candidateSlug);
                return candidateSlug;
            }
        }

        String timestampSlug = baseSlug + "-" + System.currentTimeMillis();
        log.warn("Slug 중복 해소 실패, 타임스탬프 추가: {}", timestampSlug);
        return timestampSlug;
    }

    // ========== Validation ========== //

    private void validateTitleForCreate(String title) {
        if (postRepository.existsByTitle(title)) {
            throw CustomException.fieldError("title", "이미 사용 중인 제목입니다");
        }
    }

    private void validateTitleForUpdate(Long postId, String newTitle) {
        postRepository.findByTitle(newTitle).ifPresent(existingPost -> {
            if (!existingPost.getId().equals(postId)) {
                throw CustomException.conflict("이미 존재하는 제목입니다.");
            }
        });
    }
}