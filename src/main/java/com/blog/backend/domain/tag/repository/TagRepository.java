package com.blog.backend.domain.tag.repository;

import com.blog.backend.domain.tag.entity.Tag;
import com.blog.backend.domain.tag.entity.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그명으로 태그 조회
     *
     * @param name 태그명
     * @return 태그 (Optional)
     */
    Optional<Tag> findByName(String name);

    /**
     * 태그명 목록으로 태그 일괄 조회
     * - 게시글 저장 시 기존 태그 조회용
     *
     * @param names 태그명 목록
     * @return 태그 목록
     */
    List<Tag> findByNameIn(Set<String> names);

    /**
     * 그룹별 태그 목록 조회
     *
     * @param tagGroup 태그 그룹
     * @return 해당 그룹의 태그 목록
     */
    List<Tag> findByTagGroup(TagGroup tagGroup);

    /**
     * 공개 게시글에 사용된 태그 목록 조회 (게시글 수 포함)
     * - 사이드바 태그 클라우드용
     * - 게시글 수 기준 내림차순 정렬
     *
     * @return 태그별 공개 게시글 수 (Object[]: Tag, count)
     */
    @Query("SELECT t, COUNT(p) as postCount " +
            "FROM Tag t " +
            "JOIN t.posts p " +
            "WHERE p.status = 'PUBLIC' " +
            "GROUP BY t " +
            "ORDER BY postCount DESC")
    List<Object[]> findTagsWithPublicPostCount();

    /**
     * 인기 태그 조회 (상위 N개)
     * - 공개 게시글 기준
     *
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록 (Object[]: Tag, count)
     */
    @Query("SELECT t, COUNT(p) as postCount " +
            "FROM Tag t " +
            "JOIN t.posts p " +
            "WHERE p.status = 'PUBLIC' " +
            "GROUP BY t " +
            "ORDER BY postCount DESC " +
            "LIMIT :limit")
    List<Object[]> findPopularTags(@Param("limit") int limit);

    /**
     * 태그명 존재 여부 확인
     *
     * @param name 태그명
     * @return 존재 여부
     */
    boolean existsByName(String name);
}