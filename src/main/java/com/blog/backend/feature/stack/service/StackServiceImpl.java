package com.blog.backend.feature.stack.service;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.entity.StackGroup;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.user.repository.UserRepository;
import com.blog.backend.global.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 공개 스택 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StackServiceImpl implements StackService {

    private final StackRepository stackRepository;
    private final UserRepository userRepository;

    /**
     * 전체 스택 목록 조회 (게시글 작성용)
     */
    @Override
    public List<StackResponse.StackItem> getAllStacks() {
        return stackRepository.findAll().stream()
                .map(StackResponse.StackItem::from)
                .collect(Collectors.toList());
    }

    /**
     * 그룹별 스택 + 게시글 수 목록 조회 (전체)
     */
    @Override
    public StackResponse.GroupedStacks getGroupedStacksWithPostCount() {
        List<Object[]> results = stackRepository.findStacksWithPublicPostCount();
        List<StackResponse.StackWithCount> stacksWithCount = convertToStackWithCountResponse(results);

        Map<StackGroup, List<StackResponse.StackWithCount>> groupedStacks = stacksWithCount.stream()
                .collect(Collectors.groupingBy(StackResponse.StackWithCount::getStackGroup));

        return StackResponse.GroupedStacks.of(groupedStacks);
    }

    /**
     * 그룹별 스택 + 게시글 수 목록 조회 (사용자별)
     */
    @Override
    public StackResponse.GroupedStacks getGroupedStacksWithPostCountByUser(String nickname) {
        // 사용자 존재 확인
        userRepository.findByNickname(nickname)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        List<Object[]> results = stackRepository.findStacksWithPublicPostCountByUser(nickname);
        List<StackResponse.StackWithCount> stacksWithCount = convertToStackWithCountResponse(results);

        Map<StackGroup, List<StackResponse.StackWithCount>> groupedStacks = stacksWithCount.stream()
                .collect(Collectors.groupingBy(StackResponse.StackWithCount::getStackGroup));

        return StackResponse.GroupedStacks.of(groupedStacks);
    }

    /**
     * 인기 스택 조회
     */
    @Override
    public List<StackResponse.PopularStack> getPopularStacks(int limit) {
        List<Object[]> results = stackRepository.findPopularStacks(limit);

        List<StackResponse.PopularStack> popularStacks = new ArrayList<>();
        int rank = 1;

        for (Object[] result : results) {
            Stack stack = (Stack) result[0];
            Long postCount = (Long) result[1];
            popularStacks.add(StackResponse.PopularStack.of(rank++, stack, postCount));
        }

        return popularStacks;
    }

    /**
     * 스택 자동완성 검색
     */
    @Override
    public List<StackResponse.StackItem> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return stackRepository.findByNameContainingIgnoreCaseOrderByNameAsc(
                        keyword.trim(),
                        PageRequest.of(0, 5)
                ).stream()
                .map(StackResponse.StackItem::from)
                .collect(Collectors.toList());
    }

    // ========== Private Methods ========== //

    /**
     * Object[] 결과를 StackWithCountResponse 목록으로 변환
     */
    private List<StackResponse.StackWithCount> convertToStackWithCountResponse(List<Object[]> results) {
        return results.stream()
                .map(result -> {
                    Stack stack = (Stack) result[0];
                    Long postCount = (Long) result[1];
                    return StackResponse.StackWithCount.of(stack, postCount);
                })
                .collect(Collectors.toList());
    }
}