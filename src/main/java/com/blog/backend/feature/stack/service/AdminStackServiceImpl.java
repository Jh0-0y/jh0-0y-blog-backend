package com.blog.backend.feature.stack.service;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.dto.StackRequest;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.entity.StackGroup;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.user.repository.UserRepository;
import com.blog.backend.global.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 스택 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStackServiceImpl implements AdminStackService {

    private final StackRepository stackRepository;
    private final UserRepository userRepository;

    /**
     * 스택 생성 (어드민 전용)
     */
    @Override
    @Transactional
    public StackResponse.StackItem createStack(StackRequest.Create request, Long userId) {
        validateDuplicateName(request.getName());
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        Stack stack = Stack.builder()
                .name(request.getName())
                .stackGroup(stackGroup)
                .build();

        Stack savedStack = stackRepository.save(stack);
        return StackResponse.StackItem.from(savedStack);
    }

    /**
     * 스택 수정 (어드민 전용)
     */
    @Override
    @Transactional
    public StackResponse.StackItem updateStack(Long stackId, StackRequest.Update request, Long userId) {
        Stack stack = findStackById(stackId);
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        if (!stack.getName().equals(request.getName())) {
            validateDuplicateName(request.getName());
        }

        stack.updateName(request.getName());
        if (request.getStackGroup() != null) {
            stack.updateStackGroup(stackGroup);
        }

        return StackResponse.StackItem.from(stack);
    }

    /**
     * 스택 삭제 (어드민 전용)
     */
    @Override
    @Transactional
    public void deleteStack(Long stackId, Long userId) {
        Stack stack = findStackById(stackId);
        stackRepository.delete(stack);
    }

    // ========== Private Methods ========== //

    /**
     * 스택 ID로 Stack 엔티티 조회
     */
    private Stack findStackById(Long stackId) {
        return stackRepository.findById(stackId)
                .orElseThrow(() -> CustomException.notFound("스택을 찾을 수 없습니다"));
    }

    /**
     * 스택명 중복 검사
     */
    private void validateDuplicateName(String name) {
        if (stackRepository.existsByName(name)) {
            throw CustomException.conflict("이미 존재하는 스택명입니다");
        }
    }
}