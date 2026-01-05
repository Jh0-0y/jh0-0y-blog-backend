package com.portfolio.backend.domain.post.controller;

import com.portfolio.backend.domain.post.dto.PostDto;
import com.portfolio.backend.domain.post.entity.PostCategory;
import com.portfolio.backend.domain.post.service.PostService;
import com.portfolio.backend.global.common.ApiResponse;
import com.portfolio.backend.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 생성
     * POST /api/posts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDto.DetailResponse>> createPost(
            @RequestAttribute("userId") Long userId,  // TODO: 인증 구현 후 수정
            @Valid @RequestBody PostDto.CreateRequest request
    ) {
        PostDto.DetailResponse response = postService.createPost(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "게시글이 생성되었습니다"));
    }

    /**
     * 게시글 수정
     * PUT /api/posts/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDto.DetailResponse>> updatePost(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody PostDto.UpdateRequest request
    ) {
        PostDto.DetailResponse response = postService.updatePost(userId, postId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "게시글이 수정되었습니다"));
    }

    /**
     * 게시글 삭제
     * DELETE /api/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(userId, postId);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다"));
    }

    /**
     * 게시글 상세 조회
     * GET /api/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDto.DetailResponse>> getPost(
            @PathVariable Long postId
    ) {
        PostDto.DetailResponse response = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공개 게시글 목록 조회
     * GET /api/posts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDto.ListResponse> posts = postService.getPublicPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 카테고리별 게시글 목록 조회
     * GET /api/posts/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> getPostsByCategory(
            @PathVariable PostCategory category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDto.ListResponse> posts = postService.getPostsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 태그별 게시글 목록 조회
     * GET /api/posts/tag/{tagName}
     */
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> getPostsByTag(
            @PathVariable String tagName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDto.ListResponse> posts = postService.getPostsByTag(tagName, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 게시글 검색
     * GET /api/posts/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> searchPosts(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDto.ListResponse> posts = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 내 게시글 목록 조회
     * GET /api/posts/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> getMyPosts(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDto.ListResponse> posts = postService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }
}