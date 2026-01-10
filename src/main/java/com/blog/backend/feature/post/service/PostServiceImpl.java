package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.repository.PostSpecification;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StackRepository stackRepository;

    // ========== CRUD ========== //

    @Override
    @Transactional
    public PostResponse.Detail createPost(Long userId, PostRequest.Create request) {
        User user = findUserById(userId);
        existsByTitle(request.getTitle());

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

        // 기술 스택 처리 (ID 기반 선택만)
        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        Post savedPost = postRepository.save(post);
        return PostResponse.Detail.from(savedPost, null, null);
    }

    @Override
    @Transactional
    public PostResponse.Detail updatePost(Long userId, Long postId, PostRequest.Update request) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);
        existsByTitle(request.getTitle());

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

        // 기술 스택 처리 (ID 기반 선택만)
        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        return PostResponse.Detail.from(post, null, null);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        post.clearStack();
        postRepository.delete(post);
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