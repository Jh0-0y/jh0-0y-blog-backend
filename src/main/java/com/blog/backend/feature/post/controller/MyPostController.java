package com.blog.backend.feature.post.controller;

import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.service.MyPostService;
import com.blog.backend.global.core.response.ApiResponse;
import com.blog.backend.global.core.response.PageResponse;
import com.blog.backend.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 내 게시글 컨트롤러 (인증 필수)
 *
 * 로그인한 사용자만 접근 가능한 게시글 관리 API
 * - 게시글 생성, 수정, 삭제, 복구
 */
@RestController
@RequestMapping("/api/my/posts")
@RequiredArgsConstructor
public class MyPostController {

    private final MyPostService myPostService;

    // ========== CRUD ========== //

    /**
     * 게시글 생성
     * POST /api/my/posts
     *
     * @param request 게시글 데이터 (thumbnailFileId, thumbnailUrl 포함)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse.Detail>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostRequest.Create request
    ) {
        PostResponse.Detail response = myPostService.createPost(
                userDetails.getUser(),
                request
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "게시글이 생성되었습니다"));
    }

    /**
     * 게시글 수정용 데이터 조회
     * GET /api/my/posts/{slug}/edit
     */
    @GetMapping("/{slug}/edit")
    public ResponseEntity<ApiResponse<PostResponse.Edit>> getPostForEdit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        PostResponse.Edit response = myPostService.getPostForEdit(userDetails.getUserId(), slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 수정
     * PUT /api/my/posts/{slug}
     *
     * @param slug 수정할 게시글 slug
     * @param request 게시글 데이터 (thumbnailFileId, thumbnailUrl, removeThumbnail 포함)
     */
    @PutMapping("/{slug}")
    public ResponseEntity<ApiResponse<PostResponse.Detail>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug,
            @RequestBody @Valid PostRequest.Update request
    ) {
        PostResponse.Detail response = myPostService.updatePost(
                userDetails.getUserId(),
                slug,
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response, "게시글이 수정되었습니다"));
    }

    /**
     * 게시글 삭제 (소프트 삭제)
     * DELETE /api/my/posts/{slug}
     *
     * @param slug 삭제할 게시글 slug
     */
    @DeleteMapping("/{slug}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        myPostService.deletePost(userDetails.getUserId(), slug);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다"));
    }

    /**
     * 게시글 복구
     * POST /api/my/posts/{slug}/restore
     *
     * @param slug 복구할 게시글 slug
     */
    @PostMapping("/{slug}/restore")
    public ResponseEntity<ApiResponse<Void>> restorePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        myPostService.restorePost(userDetails.getUserId(), slug);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 복구되었습니다"));
    }

    // ========== 조회 ========== //

    /**
     * 내 게시글 검색 (복합 필터링)
     * GET /api/my/posts
     *
     * - DELETED 상태 제외 (삭제된 게시글은 별도 엔드포인트)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> searchMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) String stack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofMine(postType, stack, keyword);
        Page<PostResponse.PostItems> posts = myPostService.searchMyPosts(
                userDetails.getUserId(),
                condition,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 삭제된 게시글 목록 조회
     * GET /api/my/posts/deleted
     */
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> getDeletedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "deletedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItems> posts = myPostService.getDeletedPosts(
                userDetails.getUserId(),
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }
}