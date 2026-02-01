package com.blog.backend.feature.stack.service;

import com.blog.backend.feature.stack.dto.StackResponse;

import java.util.List;

/**
 * 공개 스택 서비스 인터페이스
 *
 * 스택 조회 관련 비즈니스 로직 (인증 불필요)
 */
public interface StackService {

    /**
     * 전체 스택 목록 조회 (게시글 작성용)
     * - DB에 등록된 모든 스택 반환 (그룹 정보 포함)
     * - 게시글 수와 무관하게 전체 조회
     *
     * @return 스택 목록
     */
    List<StackResponse.StackItem> getAllStacks();

    /**
     * 그룹별 스택 목록 조회 (게시글 수 포함) - 전체 공개 게시글 기준
     * - 게시글 필터링용
     * - 실제 사용 중인 스택만 반환
     *
     * @return 그룹별로 분류된 스택 목록
     */
    StackResponse.GroupedStacks getGroupedStacksWithPostCount();

    /**
     * 그룹별 스택 목록 조회 (게시글 수 포함) - 특정 사용자 기준
     * - 사용자별 게시글 필터링용
     * - 해당 사용자가 실제 사용 중인 스택만 반환
     *
     * @param nickname 사용자 닉네임
     * @return 그룹별로 분류된 스택 목록
     */
    StackResponse.GroupedStacks getGroupedStacksWithPostCountByUser(String nickname);

    /**
     * 인기 스택 조회
     *
     * @param limit 조회할 스택 수
     * @return 인기 스택 목록 (순위 포함)
     */
    List<StackResponse.PopularStack> getPopularStacks(int limit);

    /**
     * 스택 자동완성 검색
     *
     * @param keyword 검색 키워드
     * @return 검색된 스택 목록 (최대 5개)
     */
    List<StackResponse.StackItem> autocomplete(String keyword);
}