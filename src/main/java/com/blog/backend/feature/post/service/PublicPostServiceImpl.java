package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.repository.PostSpecification;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.user.entity.User;
import com.blog.backend.feature.user.repository.UserRepository;
import com.blog.backend.global.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공개 게시글 서비스
 *
 * 인증 없이 접근 가능한 공개 게시글 관련 비즈니스 로직
 * - PUBLISHED 상태의 게시글만 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicPostServiceImpl implements PublicPostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 상세 조회 (Slug 기반)
     */
    @Override
    public PostResponse.Detail getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithStacks(slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        return buildPostDetailResponse(post);
    }

    /**
     * 공개 게시글 복합 검색
     */
    @Override
    public Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withCondition(condition), pageable)
                .map(this::buildPostItemsResponse);
    }

    /**
     * 특정 사용자의 공개 게시글 조회
     */
    @Override
    public Page<PostResponse.PostItems> getUserPublicPosts(String nickname, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.builder()
                .build();

        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> CustomException.notFound("사용자가 존재하지 않습니다."));

        return postRepository.findAll(
                PostSpecification.withUserAndCondition(user.getId(), condition)
                        .and(PostSpecification.publishedOnly()),
                pageable
        ).map(this::buildPostItemsResponse);
    }

    // ========== DTO 빌더 메서드 ========== //

    private PostResponse.PostItems buildPostItemsResponse(Post post) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

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

    private PostResponse.Detail buildPostDetailResponse(Post post) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

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

    private List<Post> getRelatedPosts(Post currentPost) {
        List<Post> relatedPosts = new ArrayList<>();

        List<String> stackNames = currentPost.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        if (stackNames.isEmpty()) {
            log.info("관련 게시글 조회 - Stack 없음, 최신 공개 게시글 조회: postId={}", currentPost.getId());
            return postRepository.findLatestPublicPosts(
                    currentPost.getId(),
                    PageRequest.of(0, 3)
            );
        }

        // 1순위: Stack 일치 + PostType 일치 (최대 2개)
        List<Post> firstPriority = postRepository.findRelatedPostsByStackAndType(
                currentPost.getId(),
                stackNames,
                currentPost.getPostType(),
                PageRequest.of(0, 2)
        );

        relatedPosts.addAll(firstPriority);
        log.info("관련 게시글 조회 - 1순위: postId={}, count={}", currentPost.getId(), firstPriority.size());

        int remaining = 3 - relatedPosts.size();

        // 2순위: Stack 일치 + PostType 다름
        if (remaining > 0) {
            List<Post> secondPriority = postRepository.findRelatedPostsByStackOnly(
                    currentPost.getId(),
                    stackNames,
                    currentPost.getPostType(),
                    PageRequest.of(0, remaining)
            );

            relatedPosts.addAll(secondPriority);
            log.info("관련 게시글 조회 - 2순위: postId={}, count={}", currentPost.getId(), secondPriority.size());

            remaining = 3 - relatedPosts.size();
        }

        // 3순위: 최신 공개 게시글
        if (remaining > 0) {
            List<Post> latestPosts = postRepository.findLatestPublicPosts(
                    currentPost.getId(),
                    PageRequest.of(0, remaining)
            );

            List<Long> existingIds = relatedPosts.stream()
                    .map(Post::getId)
                    .toList();

            List<Post> filtered = latestPosts.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .limit(remaining)
                    .toList();

            relatedPosts.addAll(filtered);
            log.info("관련 게시글 조회 - 3순위: postId={}, count={}", currentPost.getId(), filtered.size());
        }

        log.info("관련 게시글 조회 완료: postId={}, totalCount={}", currentPost.getId(), relatedPosts.size());
        return relatedPosts;
    }
}