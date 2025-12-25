package com.portfolio.backend.domain.category;

import com.portfolio.backend.domain.category.dto.CategoryRequest;
import com.portfolio.backend.domain.category.dto.CategoryResponse;
import com.portfolio.backend.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    // 전체 카테고리 조회 (게시글 수 포함)
    public List<CategoryResponse> getAllCategoriesWithPostCount() {
        return categoryRepository.findAllWithPostCount().stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long postCount = (Long) result[1];
                    return CategoryResponse.of(category, postCount);
                })
                .collect(Collectors.toList());
    }
    
    // 전체 카테고리 조회
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    // 카테고리 단건 조회
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("카테고리를 찾을 수 없습니다"));
        return CategoryResponse.from(category);
    }
    
    // 카테고리 생성
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw BusinessException.conflict("이미 존재하는 카테고리 이름입니다");
        }
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw BusinessException.conflict("이미 존재하는 슬러그입니다");
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();
        
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }
    
    // 카테고리 수정
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("카테고리를 찾을 수 없습니다"));
        
        category.update(request.getName(), request.getSlug());
        return CategoryResponse.from(category);
    }
    
    // 카테고리 삭제
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw BusinessException.notFound("카테고리를 찾을 수 없습니다");
        }
        categoryRepository.deleteById(id);
    }
}
