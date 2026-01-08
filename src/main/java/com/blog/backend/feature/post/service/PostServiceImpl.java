package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostDto;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostCategory;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.repository.PostSpecification;
import com.blog.backend.feature.tag.entity.Tag;
import com.blog.backend.feature.tag.repository.TagRepository;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
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

    // ========== CRUD ========== //

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

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            post.updateTags(tags);
        }

        Post savedPost = postRepository.save(post);
        return PostDto.DetailResponse.from(savedPost, null, null);
    }

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

        if (request.getTags() != null) {
            Set<Tag> tags = getOrCreateTags(request.getTags());
            post.updateTags(tags);
        }

        return PostDto.DetailResponse.from(post, null, null);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        post.clearTags();
        postRepository.delete(post);
    }

    @Override
    public PostDto.DetailResponse getPost(Long postId) {
        Post post = postRepository.findByIdWithTags(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        Post prevPost = postRepository.findPreviousPost(postId, PostStatus.PUBLIC).orElse(null);
        Post nextPost = postRepository.findNextPost(postId, PostStatus.PUBLIC).orElse(null);

        return PostDto.DetailResponse.from(post, prevPost, nextPost);
    }

    // ========== 복합 필터링 (신규) ========== //

    @Override
    public Page<PostDto.ListResponse> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withCondition(condition), pageable)
                .map(PostDto.ListResponse::from);
    }

    @Override
    public Page<PostDto.ListResponse> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withUserAndCondition(userId, condition), pageable)
                .map(PostDto.ListResponse::from);
    }

    // ========== 기존 메서드 (하위 호환) ========== //

    @Override
    @Deprecated
    public Page<PostDto.ListResponse> getPublicPosts(Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostDto.ListResponse> getPostsByCategory(PostCategory category, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(category, null, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostDto.ListResponse> getPostsByTag(String tagName, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, tagName, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostDto.ListResponse> searchPosts(String keyword, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, keyword);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostDto.ListResponse> getMyPosts(Long userId, Pageable pageable) {
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

    private void validateAuthor(Post post, Long userId) {
        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("게시글 수정/삭제 권한이 없습니다");
        }
    }

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