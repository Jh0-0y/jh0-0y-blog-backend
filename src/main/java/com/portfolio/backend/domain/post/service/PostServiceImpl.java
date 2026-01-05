package com.portfolio.backend.domain.post.service;

import com.portfolio.backend.domain.post.dto.PostDto;
import com.portfolio.backend.domain.post.entity.Post;
import com.portfolio.backend.domain.post.entity.PostCategory;
import com.portfolio.backend.domain.post.entity.PostStatus;
import com.portfolio.backend.domain.post.repository.PostRepository;
import com.portfolio.backend.domain.tag.entity.Tag;
import com.portfolio.backend.domain.tag.repository.TagRepository;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    // 게시글 생성
    @Override
    @Transactional
    public PostDto.DetailResponse createPost(Long userId, PostDto.CreateRequest request) {
        User user = findUserById(userId);

        Post post = Post.builder()
                .user(user)
                .category(request.getCategory())
                .title(request.getTitle())
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.PRIVATE)
                .build();

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            post.updateTags(tags);
        }

        Post savedPost = postRepository.save(post);
        return PostDto.DetailResponse.from(savedPost, null, null);
    }

    // 게시글 수정
    @Override
    @Transactional
    public PostDto.DetailResponse updatePost(Long userId, Long postId, PostDto.UpdateRequest request) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        post.update(
                request.getCategory(),
                request.getTitle(),
                request.getExcerpt(),
                request.getContent(),
                request.getStatus()
        );

        // 태그 교체
        if (request.getTags() != null) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            post.updateTags(tags);
        }

        return PostDto.DetailResponse.from(post, null, null);
    }

    // 게시글 삭제
    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        post.clearTags();
        postRepository.delete(post);
    }

    // 게시글 상세 조회
    @Override
    public PostDto.DetailResponse getPost(Long postId) {
        Post post = postRepository.findByIdWithTags(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        Post prevPost = postRepository.findPreviousPost(postId, PostStatus.PUBLIC).orElse(null);
        Post nextPost = postRepository.findNextPost(postId, PostStatus.PUBLIC).orElse(null);

        return PostDto.DetailResponse.from(post, prevPost, nextPost);
    }

    // 공개 게시글 목록 조회
    @Override
    public Page<PostDto.ListResponse> getPublicPosts(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.PUBLIC, pageable)
                .map(PostDto.ListResponse::from);
    }

    // 카테고리별 게시글 목록 조회
    @Override
    public Page<PostDto.ListResponse> getPostsByCategory(PostCategory category, Pageable pageable) {
        return postRepository.findByCategoryAndStatus(category, PostStatus.PUBLIC, pageable)
                .map(PostDto.ListResponse::from);
    }

    // 태그별 게시글 목록 조회
    @Override
    public Page<PostDto.ListResponse> getPostsByTag(String tagName, Pageable pageable) {
        return postRepository.findByTagNameAndStatus(tagName, PostStatus.PUBLIC, pageable)
                .map(PostDto.ListResponse::from);
    }

    // 게시글 검색
    @Override
    public Page<PostDto.ListResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchByKeyword(keyword, PostStatus.PUBLIC, pageable)
                .map(PostDto.ListResponse::from);
    }

    // 내 게시글 목록 조회
    @Override
    public Page<PostDto.ListResponse> getMyPosts(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable)
                .map(PostDto.ListResponse::from);
    }

    // ========== Private Methods ========== //

    /**
     * 사용자 ID로 User 엔티티 조회
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));
    }

    /**
     * 게시글 ID로 Post 엔티티 조회
     */
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    /**
     * 게시글 작성자 검증
     */
    private void validateAuthor(Post post, Long userId) {
        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("게시글 수정/삭제 권한이 없습니다");
        }
    }

    /**
     * 태그명 목록으로 Tag 엔티티 Set 조회 또는 생성
     * - 기존에 있는 태그는 조회, 없는 태그는 새로 생성
     */
    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder()
                                    .name(tagName)
                                    .build()
                    ));
            tags.add(tag);
        }

        return tags;
    }
}