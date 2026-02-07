package com.blog.backend.feature.post.util;

import com.blog.backend.global.core.exception.CustomException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 마크다운 본문 형식 검증 유틸리티
 *
 * 역할:
 * - 본문이 마크다운 형식인지 검증
 * - HTML 태그 유입 차단 (프론트엔드 변환 실패 대비)
 *
 * 검증 대상 HTML 태그:
 * - 이미지: img
 * - 레이아웃: div, span, p, section, article, header, footer
 * - 링크: a (href 속성 포함)
 * - 테이블: table, tr, td, th, thead, tbody
 * - 기타: br, hr, iframe, script, style
 */
@Slf4j
public class ValidateMarkdown {

    /**
     * 검증 대상 HTML 태그 패턴
     * <태그명 또는 <태그명> 또는 <태그명 속성...> 형태 감지
     */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
            "<(img|div|span|p|a|br|hr|table|tr|td|th|thead|tbody|section|article|header|footer|iframe|script|style)[^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 본문이 마크다운 형식인지 검증
     *
     * @param content 검증할 본문 내용
     * @throws CustomException HTML 태그가 포함된 경우 BAD_REQUEST 예외 발생
     */
    public static void validate(String content) {
        if (content == null || content.isBlank()) {
            return; // 빈 본문은 통과
        }

        if (containsHtmlTags(content)) {
            log.warn("마크다운 검증 실패 - HTML 태그 감지: content 길이={}", content.length());
            throw CustomException.badRequest("본문은 마크다운 형식만 허용됩니다. HTML 태그가 포함되어 있습니다.");
        }

        log.debug("마크다운 검증 통과: content 길이={}", content.length());
    }

    /**
     * HTML 태그 포함 여부 확인
     *
     * @param content 검사할 본문
     * @return HTML 태그 포함 시 true
     */
    public static boolean containsHtmlTags(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        return HTML_TAG_PATTERN.matcher(content).find();
    }

    /**
     * 검증 없이 HTML 태그 포함 여부만 반환 (로깅용)
     *
     * @param content 검사할 본문
     * @return HTML 태그 포함 시 true, 마크다운이면 false
     */
    public static boolean isValidMarkdown(String content) {
        return !containsHtmlTags(content);
    }
}