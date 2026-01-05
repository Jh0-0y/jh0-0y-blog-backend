package com.blog.backend.global.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {
    
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
