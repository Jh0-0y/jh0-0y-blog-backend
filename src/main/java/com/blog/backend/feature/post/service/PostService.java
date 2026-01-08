package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostDto;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.PostCategory;
import com.blog.backend.global.error.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    /**
     * 게시글 생성
     * @param userId 작성자 ID
     * @param request 게시글 생성 요청 DTO
     * @return 생성된 게시글 상세 정보
     * @throws CustomException 작성자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    PostDto.DetailResponse createPost(Long userId, PostDto.CreateRequest request);

    /**
     * 게시글 수정
     * @param userId 요청자 ID (작성자 검증용)
     * @param postId 수정할 게시글 ID
     * @param request 게시글 수정 요청 DTO
     * @return 수정된 게시글 상세 정보
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 작성자가 아닌 경우 (FORBIDDEN)
     */
    PostDto.DetailResponse updatePost(Long userId, Long postId, PostDto.UpdateRequest request);

    /**
     * 게시글 삭제
     * @param userId 요청자 ID (작성자 검증용)
     * @param postId 삭제할 게시글 ID
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 작성자가 아닌 경우 (FORBIDDEN)
     */
    void deletePost(Long userId, Long postId);

    /**
     * 게시글 상세 조회
     * @param postId 조회할 게시글 ID
     * @return 게시글 상세 정보 (이전/다음 게시글 포함)
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     */
    PostDto.DetailResponse getPost(Long postId);

    /**
     * 공개 게시글 복합 검색
     * @param condition 검색 조건 (category, tagName, keyword)
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<PostDto.ListResponse> searchPosts(PostSearchCondition condition, Pageable pageable);

    /**
     * 내 게시글 복합 검색
     * @param userId 사용자 ID
     * @param condition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<PostDto.ListResponse> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostDto.ListResponse> getPublicPosts(Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostDto.ListResponse> getPostsByCategory(PostCategory category, Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostDto.ListResponse> getPostsByTag(String tagName, Pageable pageable);

    /**
     * @deprecated Use {@link #searchPosts(PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostDto.ListResponse> searchPosts(String keyword, Pageable pageable);

    /**
     * @deprecated Use {@link #searchMyPosts(Long, PostSearchCondition, Pageable)} instead
     */
    @Deprecated
    Page<PostDto.ListResponse> getMyPosts(Long userId, Pageable pageable);
}
