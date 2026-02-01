package com.blog.backend.feature.stack.service;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.dto.StackRequest;
import com.blog.backend.global.core.exception.CustomException;

/**
 * 어드민 스택 서비스 인터페이스
 *
 * 스택 생성, 수정, 삭제 관련 비즈니스 로직
 */
public interface AdminStackService {

    /**
     * 스택 생성 (어드민 전용)
     *
     * @param request 스택 생성 요청 DTO
     * @param userId 요청 사용자 ID (권한 검증용)
     * @return 생성된 스택 정보
     * @throws CustomException 동일한 이름의 스택이 존재하는 경우 (CONFLICT)
     * @throws CustomException 잘못된 스택 그룹일 경우 (BAD_REQUEST)
     * @throws CustomException 어드민 권한이 없는 경우 (FORBIDDEN)
     */
    StackResponse.StackItem createStack(StackRequest.Create request, Long userId);

    /**
     * 스택 수정 (어드민 전용)
     *
     * @param stackId 수정할 스택 ID
     * @param request 스택 수정 요청 DTO
     * @param userId 요청 사용자 ID (권한 검증용)
     * @return 수정된 스택 정보
     * @throws CustomException 스택을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 변경하려는 이름이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 잘못된 스택 그룹일 경우 (BAD_REQUEST)
     * @throws CustomException 어드민 권한이 없는 경우 (FORBIDDEN)
     */
    StackResponse.StackItem updateStack(Long stackId, StackRequest.Update request, Long userId);

    /**
     * 스택 삭제 (어드민 전용)
     *
     * @param stackId 삭제할 스택 ID
     * @param userId 요청 사용자 ID (권한 검증용)
     * @throws CustomException 스택을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 어드민 권한이 없는 경우 (FORBIDDEN)
     */
    void deleteStack(Long stackId, Long userId);
}