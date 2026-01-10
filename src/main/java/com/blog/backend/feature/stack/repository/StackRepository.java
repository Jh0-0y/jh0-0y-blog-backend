package com.blog.backend.feature.stack.repository;

import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.entity.StackGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StackRepository extends JpaRepository<Stack, Long> {

    /**
     * 스택명으로 스택 조회
     *
     * @param name 스택명
     * @return 스택 (Optional)
     */
    Optional<Stack> findByName(String name);

    /**
     * 스택명 목록으로 스택 일괄 조회
     * - 게시글 저장 시 기존 스택 조회용
     *
     * @param names 스택명 목록
     * @return 스택 목록
     */
    List<Stack> findByNameIn(Set<String> names);

    /**
     * 그룹별 스택 목록 조회
     *
     * @param stackGroup 스택 그룹
     * @return 해당 그룹의 스택 목록
     */
    List<Stack> findByStackGroup(StackGroup stackGroup);

    /**
     * 공개 게시글에 사용된 스택 목록 조회 (게시글 수 포함)
     * - 사이드바 스택 클라우드용
     * - 게시글 수 기준 내림차순 정렬
     *
     * @return 스택별 공개 게시글 수 (Object[]: Stack, count)
     */
    @Query("SELECT s, COUNT(p) as postCount " +
            "FROM Stack s " +
            "JOIN s.posts p " +
            "WHERE p.status = 'PUBLIC' " +
            "GROUP BY s " +
            "ORDER BY postCount DESC")
    List<Object[]> findStacksWithPublicPostCount();

    /**
     * 인기 스택 조회 (상위 N개)
     * - 공개 게시글 기준
     *
     * @param limit 조회할 스택 수
     * @return 인기 스택 목록 (Object[]: Stack, count)
     */
    @Query("SELECT s, COUNT(p) as postCount " +
            "FROM Stack s " +
            "JOIN s.posts p " +
            "WHERE p.status = 'PUBLIC' " +
            "GROUP BY s " +
            "ORDER BY postCount DESC " +
            "LIMIT :limit")
    List<Object[]> findPopularStacks(@Param("limit") int limit);

    /**
     * 스택명 존재 여부 확인
     *
     * @param name 스택명
     * @return 존재 여부
     */
    boolean existsByName(String name);
}