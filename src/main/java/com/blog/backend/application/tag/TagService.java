package com.blog.backend.application.tag;

import com.blog.backend.persentation.tag.dto.TagDto;
import com.blog.backend.domain.tag.entity.TagGroup;
import com.blog.backend.global.error.CustomException;

import java.util.List;

public interface TagService {

    /**
     * 태그 생성
     *
     * <p>새로운 태그를 생성합니다. 동일한 이름의 태그가 이미 존재하면 예외가 발생합니다.</p>
     *
     * @param request 태그 생성 요청 DTO
     * @return 생성된 태그 정보
     * @throws CustomException 동일한 이름의 태그가 존재하는 경우 (CONFLICT)
     */
    TagDto.Response createTag(TagDto.CreateRequest request);

    /**
     * 태그 수정
     *
     * <p>기존 태그의 이름 또는 그룹을 수정합니다.</p>
     *
     * @param tagId 수정할 태그 ID
     * @param request 태그 수정 요청 DTO
     * @return 수정된 태그 정보
     * @throws CustomException 태그를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 변경하려는 이름이 이미 존재하는 경우 (CONFLICT)
     */
    TagDto.Response updateTag(Long tagId, TagDto.UpdateRequest request);

    /**
     * 태그 삭제
     *
     * <p>태그를 삭제합니다. 게시글에 연결된 태그 관계도 함께 제거됩니다.</p>
     *
     * @param tagId 삭제할 태그 ID
     * @throws CustomException 태그를 찾을 수 없는 경우 (NOT_FOUND)
     */
    void deleteTag(Long tagId);

    /**
     * 전체 태그 목록 조회
     *
     * <p>모든 태그 목록을 조회합니다.</p>
     *
     * @return 태그 목록
     */
    List<TagDto.Response> getAllTags();

    /**
     * 그룹별 태그 목록 조회
     *
     * <p>특정 그룹의 태그 목록을 조회합니다.</p>
     *
     * @param tagGroup 태그 그룹
     * @return 해당 그룹의 태그 목록
     */
    List<TagDto.Response> getTagsByGroup(TagGroup tagGroup);

    /**
     * 태그 + 게시글 수 목록 조회
     *
     * <p>공개 게시글이 있는 태그 목록과 각 태그별 게시글 수를 조회합니다.</p>
     * <p>사이드바의 전체 태그 클라우드에 사용됩니다.</p>
     *
     * @return 게시글 수가 포함된 태그 목록
     */
    List<TagDto.TagWithCountResponse> getTagsWithPostCount();

    /**
     * 그룹별 태그 + 게시글 수 목록 조회
     *
     * <p>태그를 그룹별로 분류하여 조회합니다.</p>
     * <p>사이드바의 All Tags 섹션에 사용됩니다.</p>
     *
     * @return 그룹별로 분류된 태그 목록
     */
    TagDto.GroupedTagsResponse getGroupedTagsWithPostCount();

    /**
     * 인기 태그 조회
     *
     * <p>공개 게시글 수가 많은 상위 태그를 조회합니다.</p>
     * <p>사이드바의 Popular Tags 섹션에 사용됩니다.</p>
     *
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록 (순위 포함)
     */
    List<TagDto.PopularTagResponse> getPopularTags(int limit);
}