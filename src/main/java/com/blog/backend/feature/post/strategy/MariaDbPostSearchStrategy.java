package com.blog.backend.feature.post.strategy;

import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.stack.entity.Stack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MariaDB 기반 게시글 검색 전략 구현체
 *
 * 제목 우선 매칭 → 부족하면 설명에서 추가 (중복 제외)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MariaDbPostSearchStrategy implements PostSearchStrategy {

    private final PostRepository postRepository;

    @Override
    public List<PostResponse.PostItems> autocomplete(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String searchKeyword = keyword.trim();
        List<Post> results = new ArrayList<>();

        // 1. 제목에서 검색
        List<Post> titleMatches = postRepository.findByTitleContainingAndPublished(
                searchKeyword,
                PageRequest.of(0, limit)
        );
        results.addAll(titleMatches);

        log.debug("자동완성 검색 - 제목 매칭: keyword={}, count={}", searchKeyword, titleMatches.size());

        // 2. 부족하면 설명에서 추가 검색 (제목 매칭 결과 제외)
        int remaining = limit - results.size();
        if (remaining > 0) {
            Set<Long> excludeIds = results.stream()
                    .map(Post::getId)
                    .collect(Collectors.toSet());

            List<Post> excerptMatches;
            if (excludeIds.isEmpty()) {
                excerptMatches = postRepository.findByExcerptContainingAndPublished(
                        searchKeyword,
                        PageRequest.of(0, remaining)
                );
            } else {
                excerptMatches = postRepository.findByExcerptContainingAndPublishedExcluding(
                        searchKeyword,
                        excludeIds,
                        PageRequest.of(0, remaining)
                );
            }

            results.addAll(excerptMatches);
            log.debug("자동완성 검색 - 설명 매칭: keyword={}, count={}", searchKeyword, excerptMatches.size());
        }

        log.info("자동완성 검색 완료: keyword={}, totalCount={}", searchKeyword, results.size());

        return results.stream()
                .map(this::toPostItems)
                .collect(Collectors.toList());
    }

    private PostResponse.PostItems toPostItems(Post post) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                post.getUser().getProfileImagePath()
        );

        return PostResponse.PostItems.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPostType(),
                post.getStatus(),
                post.getThumbnailPath(),
                tags,
                stackNames,
                author,
                post.getCreatedAt()
        );
    }
}