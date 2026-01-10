package com.blog.backend.feature.stack.service;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.dto.StackRequest;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.entity.StackGroup;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StackServiceImpl implements StackService {

    private final StackRepository stackRepository;

    // 스택 생성
    @Override
    @Transactional
    public StackResponse.Response createStack(StackRequest.Create request) {
        validateDuplicateName(request.getName());
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        Stack stack = Stack.builder()
                .name(request.getName())
                .stackGroup(stackGroup)
                .build();

        Stack savedStack = stackRepository.save(stack);
        return StackResponse.Response.from(savedStack);
    }

    // 스택 수정
    @Override
    @Transactional
    public StackResponse.Response updateStack(Long stackId, StackRequest.Update request) {
        Stack stack = findStackById(stackId);
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        if (!stack.getName().equals(request.getName())) {
            validateDuplicateName(request.getName());
        }

        stack.updateName(request.getName());
        if (request.getStackGroup() != null) {
            stack.updateStackGroup(stackGroup);
        }

        return StackResponse.Response.from(stack);
    }

    // 스택 삭제
    @Override
    @Transactional
    public void deleteStack(Long stackId) {
        Stack stack = findStackById(stackId);
        stackRepository.delete(stack);
    }

    // 전체 스택 목록 조회
    @Override
    public List<StackResponse.Response> getAllStacks() {
        return stackRepository.findAll().stream()
                .map(StackResponse.Response::from)
                .collect(Collectors.toList());
    }

    // 그룹별 스택 목록 조회
    @Override
    public List<StackResponse.Response> getStacksByGroup(StackGroup stackGroup) {
        return stackRepository.findByStackGroup(stackGroup).stream()
                .map(StackResponse.Response::from)
                .collect(Collectors.toList());
    }

    // 스택 + 게시글 수 목록 조회
    @Override
    public List<StackResponse.StackWithCount> getStacksWithPostCount() {
        List<Object[]> results = stackRepository.findStacksWithPublicPostCount();
        return convertToStackWithCountResponse(results);
    }

    // 그룹별 스택 + 게시글 수 목록 조회
    @Override
    public StackResponse.GroupedStacks getGroupedStacksWithPostCount() {
        List<StackResponse.StackWithCount> stacksWithCount = getStacksWithPostCount();

        Map<StackGroup, List<StackResponse.StackWithCount>> groupedStacks = stacksWithCount.stream()
                .collect(Collectors.groupingBy(StackResponse.StackWithCount::getStackGroup));

        return StackResponse.GroupedStacks.of(groupedStacks);
    }

    // 인기 스택 조회
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

    // ========== Private Methods ========== //

    /**
     * 스택 ID로 Stack 엔티티 조회
     */
    private Stack findStackById(Long stackId) {
        return stackRepository.findById(stackId)
                .orElseThrow(() -> CustomException.notFound("스택를 찾을 수 없습니다"));
    }

    /**
     * 스택명 중복 검사
     */
    private void validateDuplicateName(String name) {
        if (stackRepository.existsByName(name)) {
            throw CustomException.conflict("이미 존재하는 스택명입니다");
        }
    }

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