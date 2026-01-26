package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.global.core.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 공개 게시글 서비스 인터페이스
 *
 * 인증 없이 접근 가능한 공개 게시글 관련 비즈니스 로직
 * - PUBLISHED 상태의 게시글만 처리
 */
public interface PublicPostService {

    /**
     * 게시글 상세 조회 (Slug 기반)
     * - PUBLISHED 상태만 조회
     * - 관련 게시글 포함
     *
     * @param slug 조회할 게시글의 slug
     * @return 게시글 상세 정보 (관련 게시글 포함)
     * @throws CustomException 게시글을 찾을 수 없는 경우
     */
    PostResponse.Detail getPostBySlug(String slug);

    /**
     * 공개 게시글 복합 검색
     * - PUBLISHED 상태만 검색
     * - 게시글 타입, 스택, 키워드 등 다양한 조건으로 검색 가능
     *
     * @param condition 검색 조건 (postType, stackName, keyword)
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 목록
     */
    Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable);

    /**
     * 특정 사용자의 공개 게시글 조회
     * - PUBLISHED 상태만 조회
     *
     * @param nickname 조회할 사용자 닉네임
     * @param pageable 페이지네이션 정보
     * @return 해당 사용자의 공개 게시글 목록
     */
    Page<PostResponse.PostItems> getUserPublicPosts(String nickname, Pageable pageable);
}