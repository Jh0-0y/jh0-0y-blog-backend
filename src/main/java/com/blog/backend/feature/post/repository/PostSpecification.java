package com.blog.backend.feature.post.repository;

import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.tag.entity.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Post 엔티티 동적 쿼리 생성용 Specification
 */
public class PostSpecification {

    /**
     * 복합 검색 조건으로 Specification 생성
     *
     * @param condition 검색 조건 (category, tagName, keyword, status)
     * @return Specification
     */
    public static Specification<Post> withCondition(PostSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 상태 필터 (null이면 무시)
            if (condition.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), condition.getStatus()));
            }

            // 카테고리 필터 (null이면 무시)
            if (condition.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), condition.getCategory()));
            }

            // 태그 필터 (null이면 무시)
            if (condition.getTagName() != null && !condition.getTagName().isBlank()) {
                Join<Post, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(tagJoin.get("name"), condition.getTagName()));
            }

            // 키워드 검색 (null이면 무시) - 제목, 내용, 요약에서 검색
            if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
                String pattern = "%" + condition.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("content"), pattern),
                        cb.like(root.get("excerpt"), pattern)
                ));
            }

            // 중복 제거 (태그 조인 시 필요)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 특정 사용자의 게시글 + 복합 검색 조건
     *
     * @param userId 사용자 ID
     * @param condition 검색 조건
     * @return Specification
     */
    public static Specification<Post> withUserAndCondition(Long userId, PostSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 사용자 필터 (필수)
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // 상태 필터 (null이면 무시 - 내 글은 공개/비공개 모두)
            if (condition.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), condition.getStatus()));
            }

            // 카테고리 필터
            if (condition.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), condition.getCategory()));
            }

            // 태그 필터
            if (condition.getTagName() != null && !condition.getTagName().isBlank()) {
                Join<Post, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(tagJoin.get("name"), condition.getTagName()));
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
}