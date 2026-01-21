package com.blog.backend.feature.stack.service;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.dto.StackRequest;
import com.blog.backend.feature.stack.entity.StackGroup;
import com.blog.backend.global.core.exception.CustomException;

import java.util.List;

public interface StackService {

    /**
     * 스택 생성
     *
     * @param request 스택 생성 요청 DTO
     * @return 생성된 스택 정보
     * @throws CustomException 동일한 이름의 스택가 존재하는 경우 (CONFLICT)
     * @throws CustomException 잘못된 스택 그룹일 경우 (BAD_REQUEST)
     */
    StackResponse.Response createStack(StackRequest.Create request);

    /**
     * 스택 수정
     *
     * @param stackId 수정할 스택 ID
     * @param request 스택 수정 요청 DTO
     * @return 수정된 스택 정보
     * @throws CustomException 스택를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 변경하려는 이름이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 잘못된 스택 그룹일 경우 (BAD_REQUEST)
     */
    StackResponse.Response updateStack(Long stackId, StackRequest.Update request);

    /**
     * 스택 삭제
     *
     * @param stackId 삭제할 스택 ID
     * @throws CustomException 스택를 찾을 수 없는 경우 (NOT_FOUND)
     */
    void deleteStack(Long stackId);

    /**
     * 전체 스택 목록 조회
     *
     * @return 스택 목록
     */
    List<StackResponse.Response> getAllStacks();

    /**
     * 그룹별 스택 목록 조회
     *
     * @param stackGroup 스택 그룹
     * @return 해당 그룹의 스택 목록
     */
    List<StackResponse.Response> getStacksByGroup(StackGroup stackGroup);

    /**
     * 스택 + 게시글 수 목록 조회
     *
     * @return 게시글 수가 포함된 스택 목록
     */
    List<StackResponse.StackWithCount> getStacksWithPostCount();

    /**
     * 그룹별 스택 + 게시글 수 목록 조회
     *
     * @return 그룹별로 분류된 스택 목록
     */
    StackResponse.GroupedStacks getGroupedStacksWithPostCount();

    /**
     * 인기 스택 조회
     *
     * @param limit 조회할 스택 수
     * @return 인기 스택 목록 (순위 포함)
     */
    List<StackResponse.PopularStack> getPopularStacks(int limit);
}