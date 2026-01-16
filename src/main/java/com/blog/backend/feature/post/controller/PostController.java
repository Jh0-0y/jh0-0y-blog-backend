package com.blog.backend.feature.post.controller;

import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.service.PostService;
import com.blog.backend.global.common.ApiResponse;
import com.blog.backend.global.common.PageResponse;
import com.blog.backend.infra.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ========== CRUD ========== //

    /**
     * 게시글 생성 (Multipart 지원)
     * POST /api/posts
     *
     * @param data 게시글 데이터 (JSON)
     * @param thumbnail 썸네일 이미지 파일 (선택)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse.Detail>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("data") @Valid PostRequest.Create request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        PostResponse.Detail response = postService.createPost(
                userDetails.getUserId(),
                request,
                thumbnail
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "게시글이 생성되었습니다"));
    }

    /**
     * 게시글 수정 (Multipart 지원)
     * PUT /api/posts/{postId}
     *
     * @param data 게시글 데이터 (JSON)
     * @param thumbnail 새 썸네일 이미지 파일 (선택)
     */
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse.Detail>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestPart("data") @Valid PostRequest.Update request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        PostResponse.Detail response = postService.updatePost(
                userDetails.getUserId(),
                postId,
                request,
                thumbnail
        );
        return ResponseEntity.ok(ApiResponse.success(response, "게시글이 수정되었습니다"));
    }

    /**
     * 게시글 삭제
     * DELETE /api/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.deletePost(userDetails.getUserId(), postId);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다"));
    }

    /**
     * 게시글 상세 조회
     * GET /api/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse.Detail>> getPost(
            @PathVariable Long postId
    ) {
        PostResponse.Detail response = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 복합 필터링 (통합 검색 API) ========== //

    /**
     * 공개 게시글 검색 (복합 필터링)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> searchPosts(
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) String stack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(postType, stack, keyword);
        Page<PostResponse.PostItems> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 내 게시글 검색 (복합 필터링)
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> searchMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) String stack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofMine(postType, stack, keyword);
        Page<PostResponse.PostItems> posts = postService.searchMyPosts(userDetails.getUserId(), condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }
// ========== 기존 API (하위 호환, Deprecated) ========== //

    @Deprecated
    @GetMapping("/postType/{postType}")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> getPostsByCategory(
            @PathVariable PostType postType,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(postType, null, null);
        Page<PostResponse.PostItems> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    @Deprecated
    @GetMapping("/stack/{stackName}")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> getPostsByTag(
            @PathVariable String stackName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, stackName, null);
        Page<PostResponse.PostItems> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    @Deprecated
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> searchPostsByKeyword(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, keyword);
        Page<PostResponse.PostItems> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }
}