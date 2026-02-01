package com.blog.backend.feature.post.util;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 마크다운 본문에서 파일 참조를 파싱하는 유틸리티
 *
 * 지원하는 커스텀 된 마크다운 형식:
 * ::file[id=123 path=... fileName=... size=... contentType=...]::
 *
 * 역할:
 * - 본문에서 사용 중인 파일 ID 추출
 * - 중복 제거하여 Set 반환
 */
@Slf4j
public class MarkdownFileParser {

    /**
     * 마크다운 파일 참조 패턴
     * ::file[id=숫자 ...]:: 형태에서 id 값만 추출
     */
    private static final Pattern FILE_ID_PATTERN = Pattern.compile("::file\\[id=(\\d+)");

    /**
     * 마크다운 본문에서 파일 ID 추출
     *
     * 동작 방식:
     * 1. 정규식으로 ::file[id=숫자 패턴 매칭
     * 2. 매칭된 숫자를 Long으로 변환
     * 3. Set으로 중복 제거 후 반환
     *
     * 예시:
     * content = "::file[id=1 path=...]::\n::file[id=2 path=...]::\ n::file[id=1 path=...]::"
     * → {1, 2} 반환 (중복 제거)
     *
     * @param content 마크다운 본문 (null 가능)
     * @return 파일 ID 집합 (빈 Set 반환, null 아님)
     */
    public static Set<Long> extractFileIds(String content) {
        Set<Long> fileIds = new HashSet<>();

        if (content == null || content.isBlank()) {
            log.debug("본문이 비어있음 - 파일 ID 없음");
            return fileIds;
        }

        Matcher matcher = FILE_ID_PATTERN.matcher(content);

        while (matcher.find()) {
            try {
                Long fileId = Long.parseLong(matcher.group(1));
                fileIds.add(fileId);
            } catch (NumberFormatException e) {
                log.warn("파일 ID 파싱 실패: value={}", matcher.group(1), e);
                // 파싱 실패한 ID는 무시하고 계속 진행
            }
        }

        log.debug("본문에서 파일 ID 추출 완료: count={}, ids={}", fileIds.size(), fileIds);
        return fileIds;
    }

    /**
     * 본문에 파일 참조가 있는지 확인
     *
     * @param content 마크다운 본문
     * @return 파일 참조 존재 여부
     */
    public static boolean hasFileReferences(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        Matcher matcher = FILE_ID_PATTERN.matcher(content);
        return matcher.find();
    }
}