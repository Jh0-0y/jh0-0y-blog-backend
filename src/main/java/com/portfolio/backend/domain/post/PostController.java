package com.portfolio.backend.domain.post;

import com.portfolio.backend.domain.post.dto.PostDetailResponse;
import com.portfolio.backend.domain.post.dto.PostListResponse;
import com.portfolio.backend.global.common.ApiResponse;
import com.portfolio.backend.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    // 공개 글 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {
        
        PageResponse<PostListResponse> posts;
        if (categoryId != null) {
            posts = postService.getPostsByCategory(categoryId, page, size);
        } else {
            posts = postService.getPublishedPosts(page, size);
        }
        
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    // 공개 글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long id) {
        PostDetailResponse post = postService.getPublishedPost(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    // 최근 글 조회
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getRecentPosts(
            @RequestParam(defaultValue = "5") int limit) {
        List<PostListResponse> posts = postService.getRecentPosts(limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
}
