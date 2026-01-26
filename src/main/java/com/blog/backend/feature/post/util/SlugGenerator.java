package com.blog.backend.feature.post.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 게시글 제목을 URL-safe한 slug로 변환하는 유틸리티
 *
 * 특징:
 * - 한글 지원 (영문 변환 없이 한글 그대로 유지)
 * - 공백 → 하이픈(-) 변환
 * - 특수문자 제거
 * - 연속된 하이픈 정리
 * - 최대 길이 제한 (150자)
 */
@Slf4j
public class SlugGenerator {

    private static final int MAX_SLUG_LENGTH = 150;
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");
    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9가-힣\\s-]");

    /**
     * 제목으로부터 slug 생성
     *
     * 예시:
     * - "Spring Boot 시작하기" → "spring-boot-시작하기"
     * - "React 입문 가이드!!!" → "react-입문-가이드"
     * - "Java의 Stream API" → "java의-stream-api"
     *
     * @param title 게시글 제목
     * @return URL-safe한 slug
     */
    public static String generate(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목이 비어있습니다.");
        }

        String slug = title.trim();

        // 2. 소문자 변환 (영문만 해당, 한글은 그대로)
        slug = slug.toLowerCase();

        // 3. 공백을 하이픈으로 변환
        slug = slug.replaceAll("\\s+", "-");

        // 4. 허용되지 않는 문자 제거 (영문, 숫자, 한글, 하이픈만 허용)
        slug = INVALID_CHARS.matcher(slug).replaceAll("");

        // 5. 연속된 하이픈을 하나로 통합
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");

        // 6. 앞뒤 하이픈 제거
        slug = slug.replaceAll("^-+|-+$", "");

        // 7. 최대 길이 제한
        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH);
            // 마지막이 하이픈으로 끝나면 제거
            slug = slug.replaceAll("-+$", "");
        }

        // 8. 빈 문자열 방지
        if (slug.isBlank()) {
            throw new IllegalArgumentException("유효한 slug를 생성할 수 없습니다: " + title);
        }

        log.debug("Slug 생성: '{}' → '{}'", title, slug);
        return slug;
    }

    /**
     * 중복된 slug에 번호를 추가
     *
     * 예시:
     * - generateWithSuffix("react-입문-가이드", 2) → "react-입문-가이드-2"
     * - generateWithSuffix("react-입문-가이드", 10) → "react-입문-가이드-10"
     *
     * @param baseSlug 기본 slug
     * @param count 중복 횟수
     * @return 번호가 추가된 slug
     */
    public static String generateWithSuffix(String baseSlug, int count) {
        String suffix = "-" + count;

        // 최대 길이를 초과하면 baseSlug를 줄임
        int maxBaseLength = MAX_SLUG_LENGTH - suffix.length();
        if (baseSlug.length() > maxBaseLength) {
            baseSlug = baseSlug.substring(0, maxBaseLength);
            baseSlug = baseSlug.replaceAll("-+$", "");
        }

        return baseSlug + suffix;
    }

    /**
     * slug 유효성 검증
     *
     * @param slug 검증할 slug
     * @return 유효하면 true
     */
    public static boolean isValid(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }

        // 길이 체크
        if (slug.length() > MAX_SLUG_LENGTH) {
            return false;
        }

        // 허용된 문자만 포함하는지 체크
        return slug.matches("^[a-z0-9가-힣-]+$");
    }
}