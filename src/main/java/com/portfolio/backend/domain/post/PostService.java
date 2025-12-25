package com.portfolio.backend.domain.post;

import com.portfolio.backend.domain.category.Category;
import com.portfolio.backend.domain.category.CategoryRepository;
import com.portfolio.backend.domain.post.dto.PostDetailResponse;
import com.portfolio.backend.domain.post.dto.PostListResponse;
import com.portfolio.backend.domain.post.dto.PostRequest;
import com.portfolio.backend.domain.tag.Tag;
import com.portfolio.backend.domain.tag.TagService;
import com.portfolio.backend.domain.user.User;
import com.portfolio.backend.domain.user.UserRepository;
import com.portfolio.backend.global.common.PageResponse;
import com.portfolio.backend.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;
    
    // 공개 글 목록 조회 (페이징)
    public PageResponse<PostListResponse> getPublishedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findByIsPublishedTrue(pageable);
        
        Page<PostListResponse> responsePage = postPage.map(PostListResponse::from);
        return PageResponse.from(responsePage);
    }
    
    // 카테고리별 공개 글 목록 조회
    public PageResponse<PostListResponse> getPostsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findByCategoryIdAndIsPublishedTrue(categoryId, pageable);
        
        Page<PostListResponse> responsePage = postPage.map(PostListResponse::from);
        return PageResponse.from(responsePage);
    }
    
    // 공개 글 상세 조회
    public PostDetailResponse getPublishedPost(Long id) {
        Post post = postRepository.findPublishedByIdWithDetails(id)
                .orElseThrow(() -> BusinessException.notFound("글을 찾을 수 없습니다"));
        return PostDetailResponse.from(post);
    }
    
    // 글 상세 조회 (관리자용 - 비공개 포함)
    public PostDetailResponse getPost(Long id) {
        Post post = postRepository.findByIdWithDetails(id)
                .orElseThrow(() -> BusinessException.notFound("글을 찾을 수 없습니다"));
        return PostDetailResponse.from(post);
    }
    
    // 최근 글 조회
    public List<PostListResponse> getRecentPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findRecentPosts(pageable).stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());
    }
    
    // 글 생성
    @Transactional
    public PostDetailResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> BusinessException.notFound("카테고리를 찾을 수 없습니다"));
        }
        
        Post post = Post.builder()
                .user(user)
                .category(category)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .isPublished(request.isPublished())
                .build();
        
        // 태그 처리
        Set<Tag> tags = tagService.getOrCreateTags(request.getTags());
        post.updateTags(tags);
        
        Post saved = postRepository.save(post);
        return PostDetailResponse.from(saved);
    }
    
    // 글 수정
    @Transactional
    public PostDetailResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findByIdWithDetails(id)
                .orElseThrow(() -> BusinessException.notFound("글을 찾을 수 없습니다"));
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> BusinessException.notFound("카테고리를 찾을 수 없습니다"));
        }
        
        post.update(
                request.getTitle(),
                request.getContent(),
                request.getSummary(),
                category,
                request.isPublished()
        );
        
        // 태그 업데이트
        Set<Tag> tags = tagService.getOrCreateTags(request.getTags());
        post.updateTags(tags);
        
        return PostDetailResponse.from(post);
    }
    
    // 글 삭제
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw BusinessException.notFound("글을 찾을 수 없습니다");
        }
        postRepository.deleteById(id);
    }
}
