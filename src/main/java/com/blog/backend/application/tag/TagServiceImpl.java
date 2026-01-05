package com.blog.backend.application.tag;

import com.blog.backend.persentation.tag.dto.TagDto;
import com.blog.backend.domain.tag.entity.Tag;
import com.blog.backend.domain.tag.entity.TagGroup;
import com.blog.backend.domain.tag.repository.TagRepository;
import com.blog.backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    // 태그 생성
    @Override
    @Transactional
    public TagDto.Response createTag(TagDto.CreateRequest request) {
        validateDuplicateName(request.getName());

        Tag tag = Tag.builder()
                .name(request.getName())
                .tagGroup(request.getTagGroup())
                .build();

        Tag savedTag = tagRepository.save(tag);
        return TagDto.Response.from(savedTag);
    }

    // 태그 수정
    @Override
    @Transactional
    public TagDto.Response updateTag(Long tagId, TagDto.UpdateRequest request) {
        Tag tag = findTagById(tagId);

        // 이름 변경 시 중복 검사
        if (!tag.getName().equals(request.getName())) {
            validateDuplicateName(request.getName());
        }

        tag.updateName(request.getName());
        if (request.getTagGroup() != null) {
            tag.updateTagGroup(request.getTagGroup());
        }

        return TagDto.Response.from(tag);
    }

    // 태그 삭제
    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = findTagById(tagId);
        tagRepository.delete(tag);
    }

    // 전체 태그 목록 조회
    @Override
    public List<TagDto.Response> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagDto.Response::from)
                .collect(Collectors.toList());
    }

    // 그룹별 태그 목록 조회
    @Override
    public List<TagDto.Response> getTagsByGroup(TagGroup tagGroup) {
        return tagRepository.findByTagGroup(tagGroup).stream()
                .map(TagDto.Response::from)
                .collect(Collectors.toList());
    }

    // 태그 + 게시글 수 목록 조회
    @Override
    public List<TagDto.TagWithCountResponse> getTagsWithPostCount() {
        List<Object[]> results = tagRepository.findTagsWithPublicPostCount();
        return convertToTagWithCountResponse(results);
    }

    // 그룹별 태그 + 게시글 수 목록 조회
    @Override
    public TagDto.GroupedTagsResponse getGroupedTagsWithPostCount() {
        List<TagDto.TagWithCountResponse> tagsWithCount = getTagsWithPostCount();

        Map<TagGroup, List<TagDto.TagWithCountResponse>> groupedTags = tagsWithCount.stream()
                .collect(Collectors.groupingBy(TagDto.TagWithCountResponse::getTagGroup));

        return TagDto.GroupedTagsResponse.of(groupedTags);
    }

    // 인기 태그 조회
    @Override
    public List<TagDto.PopularTagResponse> getPopularTags(int limit) {
        List<Object[]> results = tagRepository.findPopularTags(limit);

        List<TagDto.PopularTagResponse> popularTags = new ArrayList<>();
        int rank = 1;

        for (Object[] result : results) {
            Tag tag = (Tag) result[0];
            Long postCount = (Long) result[1];
            popularTags.add(TagDto.PopularTagResponse.of(rank++, tag, postCount));
        }

        return popularTags;
    }

    // ========== Private Methods ========== //

    /**
     * 태그 ID로 Tag 엔티티 조회
     */
    private Tag findTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> CustomException.notFound("태그를 찾을 수 없습니다"));
    }

    /**
     * 태그명 중복 검사
     */
    private void validateDuplicateName(String name) {
        if (tagRepository.existsByName(name)) {
            throw CustomException.conflict("이미 존재하는 태그명입니다");
        }
    }

    /**
     * Object[] 결과를 TagWithCountResponse 목록으로 변환
     */
    private List<TagDto.TagWithCountResponse> convertToTagWithCountResponse(List<Object[]> results) {
        return results.stream()
                .map(result -> {
                    Tag tag = (Tag) result[0];
                    Long postCount = (Long) result[1];
                    return TagDto.TagWithCountResponse.of(tag, postCount);
                })
                .collect(Collectors.toList());
    }
}