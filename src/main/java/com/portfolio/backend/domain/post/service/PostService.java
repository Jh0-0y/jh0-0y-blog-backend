package com.portfolio.backend.domain.post.service;

import com.portfolio.backend.domain.post.dto.PostDto;
import com.portfolio.backend.domain.post.entity.PostCategory;
import com.portfolio.backend.global.error.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    /**
     * 게시글 생성
     *
     * <p>새로운 게시글을 생성합니다. 태그가 포함된 경우 기존 태그를 찾거나 새로 생성하여 연결합니다.</p>
     *
     * <h3>처리 흐름:</h3>
     * <ol>
     *   <li>요청 데이터 검증</li>
     *   <li>작성자(User) 조회</li>
     *   <li>Post 엔티티 생성</li>
     *   <li>태그 처리 (기존 태그 조회 또는 신규 생성)</li>
     *   <li>게시글 저장</li>
     * </ol>
     *
     * @param userId 작성자 ID
     * @param request 게시글 생성 요청 DTO
     * @return 생성된 게시글 상세 정보
     * @throws CustomException 작성자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    PostDto.DetailResponse createPost(Long userId, PostDto.CreateRequest request);

    /**
     * 게시글 수정
     *
     * <p>기존 게시글의 내용을 수정합니다. 태그 변경 시 기존 태그 연결을 해제하고 새 태그로 교체합니다.</p>
     *
     * <h3>처리 흐름:</h3>
     * <ol>
     *   <li>게시글 조회</li>
     *   <li>작성자 권한 검증</li>
     *   <li>게시글 정보 수정</li>
     *   <li>태그 교체</li>
     * </ol>
     *
     * @param userId 요청자 ID (작성자 검증용)
     * @param postId 수정할 게시글 ID
     * @param request 게시글 수정 요청 DTO
     * @return 수정된 게시글 상세 정보
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 작성자가 아닌 경우 (FORBIDDEN)
     */
    PostDto.DetailResponse updatePost(Long userId, Long postId, PostDto.UpdateRequest request);

    /**
     * 게시글 삭제
     *
     * <p>게시글을 삭제합니다. 연결된 태그 관계도 함께 제거됩니다.</p>
     *
     * @param userId 요청자 ID (작성자 검증용)
     * @param postId 삭제할 게시글 ID
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 작성자가 아닌 경우 (FORBIDDEN)
     */
    void deletePost(Long userId, Long postId);

    /**
     * 게시글 상세 조회
     *
     * <p>게시글의 상세 정보를 조회합니다. 이전/다음 게시글 정보도 함께 반환합니다.</p>
     *
     * <h3>포함 정보:</h3>
     * <ul>
     *   <li>게시글 전체 내용</li>
     *   <li>연결된 태그 목록</li>
     *   <li>이전/다음 게시글 (공개 게시글 기준)</li>
     * </ul>
     *
     * @param postId 조회할 게시글 ID
     * @return 게시글 상세 정보 (이전/다음 게시글 포함)
     * @throws CustomException 게시글을 찾을 수 없는 경우 (NOT_FOUND)
     */
    PostDto.DetailResponse getPost(Long postId);

    /**
     * 공개 게시글 목록 조회
     *
     * <p>공개 상태의 게시글 목록을 페이징하여 조회합니다.</p>
     *
     * @param pageable 페이징 정보 (page, size, sort)
     * @return 공개 게시글 목록 페이지
     */
    Page<PostDto.ListResponse> getPublicPosts(Pageable pageable);

    /**
     * 카테고리별 게시글 목록 조회
     *
     * <p>특정 카테고리의 공개 게시글 목록을 조회합니다.</p>
     *
     * @param category 카테고리
     * @param pageable 페이징 정보
     * @return 해당 카테고리의 공개 게시글 목록 페이지
     */
    Page<PostDto.ListResponse> getPostsByCategory(PostCategory category, Pageable pageable);

    /**
     * 태그별 게시글 목록 조회
     *
     * <p>특정 태그가 포함된 공개 게시글 목록을 조회합니다.</p>
     *
     * @param tagName 태그명
     * @param pageable 페이징 정보
     * @return 해당 태그가 포함된 공개 게시글 목록 페이지
     */
    Page<PostDto.ListResponse> getPostsByTag(String tagName, Pageable pageable);

    /**
     * 게시글 검색
     *
     * <p>제목 또는 내용에 키워드가 포함된 공개 게시글을 검색합니다.</p>
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<PostDto.ListResponse> searchPosts(String keyword, Pageable pageable);

    /**
     * 내 게시글 목록 조회 (관리자용)
     *
     * <p>특정 사용자가 작성한 모든 게시글(공개/비공개)을 조회합니다.</p>
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 해당 사용자의 모든 게시글 목록 페이지
     */
    Page<PostDto.ListResponse> getMyPosts(Long userId, Pageable pageable);
}