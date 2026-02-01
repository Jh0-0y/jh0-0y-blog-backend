package com.blog.backend.feature.post.repository;

import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.stack.entity.Stack;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Post 엔티티 동적 쿼리 생성용 Specification
 *
 * - 공개 조회: PUBLISHED 상태만 대상
 * - 내 게시글 조회: 상태값 파라미터로 필터링 가능
 */
public class PostSpecification {

    /**
     * 공개 게시글 복합 검색 조건 (PUBLISHED만)
     *
     * @param condition 검색 조건 (nickname, postType, stackName, keyword)
     * @return Specification
     */
    public static Specification<Post> withCondition(PostSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 공개된 게시글만 (필수)
            predicates.add(cb.equal(root.get("status"), PostStatus.PUBLISHED));

            // 작성자 닉네임 필터
            if (condition.getNickname() != null && !condition.getNickname().isBlank()) {
                predicates.add(cb.equal(root.get("user").get("nickname"), condition.getNickname()));
            }

            // 게시글 타입 필터
            if (condition.getPostType() != null) {
                predicates.add(cb.equal(root.get("postType"), condition.getPostType()));
            }

            // 스택 필터
            if (condition.getStackName() != null && !condition.getStackName().isBlank()) {
                Join<Post, Stack> stackJoin = root.join("stacks", JoinType.INNER);
                predicates.add(cb.equal(stackJoin.get("name"), condition.getStackName()));
            }

            // 키워드 검색 - 제목, 요약에서 검색
            if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
                String pattern = "%" + condition.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("excerpt"), pattern)
                ));
            }

            // 중복 제거 (스택 조인 시 필요)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 특정 사용자의 게시글 + 복합 검색 조건 (상태별 필터링 가능)
     *
     * @param userId 사용자 ID
     * @param condition 검색 조건 (status 포함)
     * @return Specification
     */
    public static Specification<Post> withUserAndCondition(Long userId, PostSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 사용자 필터 (필수)
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // 게시글 타입 필터
            if (condition.getPostType() != null) {
                predicates.add(cb.equal(root.get("postType"), condition.getPostType()));
            }

            // 스택 필터
            if (condition.getStackName() != null && !condition.getStackName().isBlank()) {
                Join<Post, Stack> stackJoin = root.join("stacks", JoinType.INNER);
                predicates.add(cb.equal(stackJoin.get("name"), condition.getStackName()));
            }

            // 키워드 검색
            if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
                String pattern = "%" + condition.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("content"), pattern),
                        cb.like(root.get("excerpt"), pattern)
                ));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * PUBLISHED 상태만 조회 (공개 게시글용)
     */
    public static Specification<Post> publishedOnly() {
        return (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED);
    }

    /**
     * 특정 사용자의 게시글 필터
     */
    public static Specification<Post> byUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
}