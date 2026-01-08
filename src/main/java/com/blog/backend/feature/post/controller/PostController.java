package com.blog.backend.feature.post.controller;

import com.blog.backend.feature.post.dto.PostDto;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.PostCategory;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ========== CRUD ========== //

    /**
     * 게시글 생성
     * POST /api/posts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDto.DetailResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostDto.CreateRequest request
    ) {
        PostDto.DetailResponse response = postService.createPost(userDetails.getUserId(), request);
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody PostDto.UpdateRequest request
    ) {
        PostDto.DetailResponse response = postService.updatePost(userDetails.getUserId(), postId, request);
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
    public ResponseEntity<ApiResponse<PostDto.DetailResponse>> getPost(
            @PathVariable Long postId
    ) {
        PostDto.DetailResponse response = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== 복합 필터링 (통합 검색 API) ========== //

    /**
     * 공개 게시글 검색 (복합 필터링)
     *
     * GET /api/posts
     * GET /api/posts?category=CORE
     * GET /api/posts?tag=Spring
     * GET /api/posts?keyword=JPA
     * GET /api/posts?category=CORE&tag=Spring
     * GET /api/posts?category=CORE&tag=Spring&keyword=JPA
     *
     * @param category 카테고리 (optional)
     * @param tag 태그명 (optional)
     * @param keyword 검색 키워드 (optional)
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> searchPosts(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(category, tag, keyword);
        Page<PostDto.ListResponse> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 내 게시글 검색 (복합 필터링)
     *
     * GET /api/posts/my
     * GET /api/posts/my?category=CORE
     * GET /api/posts/my?tag=Spring
     * GET /api/posts/my?category=CORE&tag=Spring&keyword=JPA
     *
     * @param userDetails 인증된 사용자 정보
     * @param category 카테고리 (optional)
     * @param tag 태그명 (optional)
     * @param keyword 검색 키워드 (optional)
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> searchMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofMine(category, tag, keyword);
        Page<PostDto.ListResponse> posts = postService.searchMyPosts(userDetails.getUserId(), condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    // ========== 기존 API (하위 호환, Deprecated) ========== //

    /**
     * @deprecated Use GET /api/posts?category={category} instead
     */
    @Deprecated
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> getPostsByCategory(
            @PathVariable PostCategory category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(category, null, null);
        Page<PostDto.ListResponse> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * @deprecated Use GET /api/posts?tag={tagName} instead
     */
    @Deprecated
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> getPostsByTag(
            @PathVariable String tagName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, tagName, null);
        Page<PostDto.ListResponse> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * @deprecated Use GET /api/posts?keyword={keyword} instead
     */
    @Deprecated
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostDto.ListResponse>>> searchPostsByKeyword(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, keyword);
        Page<PostDto.ListResponse> posts = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }
}