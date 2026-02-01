package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.global.core.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 공개 게시글 서비스 인터페이스
 *
 * 인증 없이 접근 가능한 공개 게시글 관련 비즈니스 로직
 * - PUBLISHED 상태의 게시글만 처리
 */
public interface PublicPostService {

    /**
     * 게시글 상세 조회 (Nickname + Slug 기반)
     * - PUBLISHED 상태만 조회
     * - 작성자 검증 포함
     * - 관련 게시글 포함
     *
     * @param nickname 작성자 닉네임
     * @param slug 조회할 게시글의 slug
     * @return 게시글 상세 정보 (관련 게시글 포함)
     * @throws CustomException 게시글을 찾을 수 없거나 작성자가 일치하지 않는 경우
     */
    PostResponse.Detail getPostByNicknameAndSlug(String nickname, String slug);

    /**
     * 공개 게시글 복합 검색
     * - PUBLISHED 상태만 검색
     * - 게시글 타입, 스택, 키워드, 작성자 등 다양한 조건으로 검색 가능
     *
     * @param condition 검색 조건 (nickname, postType, stackName, keyword)
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 목록
     */
    Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable);

    /**
     * 자동완성 검색
     * - 제목 우선 매칭 후 부족하면 설명에서 추가
     * - PUBLISHED 상태만 검색
     *
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록 (최대 10개)
     */
    List<PostResponse.PostItems> autocomplete(String keyword);
}