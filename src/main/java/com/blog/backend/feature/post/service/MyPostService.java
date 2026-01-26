package com.blog.backend.feature.post.service;

import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.user.entity.User;
import com.blog.backend.global.core.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 내 게시글 서비스
 *
 * 인증된 사용자의 게시글 관리 비즈니스 로직
 * - 게시글 생성, 수정, 삭제, 복구
 * - 본인의 모든 상태 게시글 조회 가능
 */
public interface MyPostService {

    /**
     * 게시글 생성
     * - 썸네일 파일 ID와 URL을 함께 전달받아 처리
     * - 본문에서 파일 ID를 파싱하여 자동 매핑
     * - Slug는 제목 기반 자동 생성 (중복 시 번호 추가)
     *
     * @param user 게시글 작성자
     * @param request 게시글 생성 요청 데이터
     * @return PostResponse.Detail 생성된 게시글 상세 정보
     * @throws CustomException 제목 중복, 사용자 없음, 파일 없음 등
     */
    PostResponse.Detail createPost(User user, PostRequest.Create request);

    /**
     * 게시글 수정용 데이터 조회
     * - 본인 게시글은 상태 무관하게 조회 가능
     * - relatedPosts 없이 가벼운 데이터 반환
     *
     * @param userId 조회 요청 사용자 ID
     * @param slug 조회할 게시글의 slug
     * @return PostResponse.Edit 게시글 수정용 데이터
     * @throws CustomException 게시글을 찾을 수 없거나 권한이 없는 경우
     */
    PostResponse.Edit getPostForEdit(Long userId, String slug);

    /**
     * 게시글 수정
     * - 제목 변경 시 slug 자동 재생성
     * - 썸네일 교체 또는 제거 가능
     * - 본문 파일 매핑 차집합 기반 자동 갱신
     *
     * @param userId 수정 요청 사용자 ID
     * @param slug 수정할 게시글의 slug
     * @param request 게시글 수정 요청 데이터
     * @return PostResponse.Detail 수정된 게시글 상세 정보 (relatedPosts 포함)
     * @throws CustomException 게시글 없음, 권한 없음, 제목 중복 등
     */
    PostResponse.Detail updatePost(Long userId, String slug, PostRequest.Update request);

    /**
     * 게시글 삭제 (소프트 삭제)
     * - 상태를 DELETED로 변경하고 deletedAt 기록
     * - 실제 데이터는 DB에 유지
     *
     * @param userId 삭제 요청 사용자 ID
     * @param slug 삭제할 게시글의 slug
     * @throws CustomException 게시글 없음, 권한 없음
     */
    void deletePost(Long userId, String slug);

    /**
     * 게시글 복구
     * - DELETED 상태의 게시글을 PUBLISHED 상태로 복구
     * - deletedAt 초기화
     *
     * @param userId 복구 요청 사용자 ID
     * @param slug 복구할 게시글의 slug
     * @throws CustomException 게시글 없음, 권한 없음, 이미 복구된 게시글
     */
    void restorePost(Long userId, String slug);

    /**
     * 내 게시글 검색
     * - 본인의 게시글 조회
     * - 게시글 타입, 스택, 키워드 등 다양한 조건으로 검색 가능
     *
     * @param userId 조회 요청 사용자 ID
     * @param condition 검색 조건 (postType, stackName, keyword)
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 목록
     */
    Page<PostResponse.PostItems> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable);

    /**
     * 삭제된 게시글 조회
     * - DELETED 상태의 게시글만 조회
     * - 복구 기능을 위해 제공
     *
     * @param userId 조회 요청 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return Page<PostResponse.PostItems> 삭제된 게시글 목록
     */
    Page<PostResponse.PostItems> getDeletedPosts(Long userId, Pageable pageable);
}