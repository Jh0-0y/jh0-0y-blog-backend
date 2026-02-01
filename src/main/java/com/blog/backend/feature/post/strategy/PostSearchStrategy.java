package com.blog.backend.feature.post.strategy;

import com.blog.backend.feature.post.dto.PostResponse;

import java.util.List;

/**
 * 게시글 검색 전략 인터페이스
 *
 * 향후 Redis, Elasticsearch 등 다른 검색 엔진 도입 시
 * 이 인터페이스를 구현하여 교체 가능
 */
public interface PostSearchStrategy {

    /**
     * 자동완성 검색
     * - 제목 우선 매칭 후 부족하면 설명에서 추가
     * - 중복 제외
     *
     * @param keyword 검색 키워드
     * @param limit 최대 결과 수
     * @return 검색된 게시글 목록
     */
    List<PostResponse.PostItems> autocomplete(String keyword, int limit);
}