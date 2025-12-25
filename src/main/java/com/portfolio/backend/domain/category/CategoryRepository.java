package com.portfolio.backend.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
    
    // 카테고리별 게시글 수 조회
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN Post p ON p.category = c AND p.isPublished = true GROUP BY c ORDER BY c.name")
    List<Object[]> findAllWithPostCount();
}
