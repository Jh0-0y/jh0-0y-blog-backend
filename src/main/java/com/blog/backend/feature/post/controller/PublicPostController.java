package com.blog.backend.feature.post.controller;

import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.service.PublicPostService;
import com.blog.backend.global.core.response.ApiResponse;
import com.blog.backend.global.core.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공개 게시글 컨트롤러 (인증 불필요)
 *
 * 모든 사용자가 접근 가능한 공개 게시글 관련 API
 * - PUBLISHED 상태의 게시글만 조회 가능
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PublicPostController {

    private final PublicPostService publicPostService;

    /**
     * 게시글 상세 조회 (Nickname + Slug 기반)
     * GET /api/posts/{nickname}/{slug}
     *
     * @param nickname 작성자 닉네임
     * @param slug 게시글 slug
     * @return 게시글 상세 정보 (관련 게시글 포함)
     */
    @GetMapping("/{nickname}/{slug}")
    public ResponseEntity<ApiResponse<PostResponse.Detail>> getPostByNicknameAndSlug(
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        PostResponse.Detail response = publicPostService.getPostByNicknameAndSlug(nickname, slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공개 게시글 검색 (복합 필터링)
     * GET /api/posts
     *
     * 쿼리 파라미터:
     * - postType: 게시글 타입 (선택)
     * - stack: 스택명 (선택)
     * - keyword: 검색어 (선택) - 제목, 요약에서 검색
     * - page, size, sort
     *
     * @return 공개 게시글 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> searchPosts(
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) String stack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(postType, stack, keyword);
        Page<PostResponse.PostItems> posts = publicPostService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 특정 사용자의 공개된 게시글 조회 (복합 필터링)
     * GET /api/posts/user/{nickname}
     *
     * 쿼리 파라미터:
     * - postType: 게시글 타입 (선택)
     * - stack: 스택명 (선택)
     * - keyword: 검색어 (선택) - 제목, 요약에서 검색
     * - page, size, sort
     *
     * @param nickname 사용자 닉네임
     * @return 해당 사용자의 공개 게시글 목록
     */
    @GetMapping("/user/{nickname}")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse.PostItems>>> getUserPublicPosts(
            @PathVariable String nickname,
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) String stack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofUser(nickname, postType, stack, keyword);
        Page<PostResponse.PostItems> posts = publicPostService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    /**
     * 게시글 자동완성 검색
     * GET /api/posts/autocomplete?keyword=검색어
     *
     * - 제목 우선 매칭 후 부족하면 설명에서 추가
     * - PUBLISHED 상태만 검색
     * - 최대 10개 반환
     *
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<PostResponse.PostItems>>> autocomplete(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        List<PostResponse.PostItems> results = publicPostService.autocomplete(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}