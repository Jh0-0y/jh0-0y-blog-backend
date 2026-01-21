package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.global.core.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {

    // ========== CRUD ========== //

    /**
     * 게시글 생성 (썸네일 포함)
     *
     * @param userId 작성자 ID
     * @param request 게시글 생성 요청 DTO
     * @param thumbnail 썸네일 이미지 파일 (선택)
     * @return 생성된 게시글 상세 정보
     * @throws CustomException 작성자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 제목이 중복될 경우 (CONFLICT)
     */
    PostResponse.Detail createPost(Long userId, PostRequest.Create request, MultipartFile thumbnail);

    /**
     * 게시글 수정 (썸네일 포함) - Slug 기반
     *
     * @param userId 요청자 ID (작성자 검증용)
     * @param slug 수정할 게시글 slug
     * @param request 게시글 수정 요청 DTO
     * @param thumbnail 새 썸네일 이미지 파일 (선택)
     * @return 수정된 게시글 상세 정보
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 작성자가 아닌 경우 (FORBIDDEN)
     * @throws CustomException 제목이 중복될 경우 (CONFLICT)
     */
    PostResponse.Detail updatePostBySlug(Long userId, String slug, PostRequest.Update request, MultipartFile thumbnail);

    /**
     * 게시글 삭제 - Slug 기반
     *
     * @param userId 요청자 ID (작성자 검증용)
     * @param slug 삭제할 게시글 slug
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 작성자가 아닌 경우 (FORBIDDEN)
     */
    void deletePostBySlug(Long userId, String slug);

    /**
     * 게시글 상세 조회 (Slug 기반) - 메인 조회 방식
     * - 관련 게시글 3개 포함
     *
     * @param slug 조회할 게시글 slug
     * @return 게시글 상세 정보 (관련 게시글 포함)
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     */
    PostResponse.Detail getPostBySlug(String slug);

    // ========== 복합 필터링 (통합 검색 API) ========== //

    /**
     * 공개 게시글 복합 검색
     *
     * @param condition 검색 조건 (postType, stackName, keyword)
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable);

    /**
     * 내 게시글 복합 검색
     *
     * @param userId 사용자 ID
     * @param condition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<PostResponse.PostItems> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable);

    // ========== 기존 API (하위 호환, Deprecated) ========== //

    /**
     * @deprecated Use {@link #updatePostBySlug(Long, String, PostRequest.Update, MultipartFile)} instead
     */
    @Deprecated
    PostResponse.Detail updatePost(Long userId, Long postId, PostRequest.Update request, MultipartFile thumbnail);

    /**
     * @deprecated Use {@link #deletePostBySlug(Long, String)} instead
     */
    @Deprecated
    void deletePost(Long userId, Long postId);

    /**
     * @deprecated Use {@link #getPostBySlug(String)} instead
     */
    @Deprecated
    PostResponse.Detail getPost(Long postId);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostResponse.PostItems> getPublicPosts(Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostResponse.PostItems> getPostsByPostType(PostType postType, Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostResponse.PostItems> getPostsByTag(String tagName, Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostResponse.PostItems> searchPosts(String keyword, Pageable pageable);

    /**
     * @deprecated Use {@link #searchMyPosts(Long, PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostResponse.PostItems> getMyPosts(Long userId, Pageable pageable);
}