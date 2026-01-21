package com.blog.backend.feature.auth.entity;

import com.blog.backend.global.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User와 FileMetadata 간의 매핑 테이블
 *
 * 설계 의도:
 * - User와 File은 독립적으로 관리되며, 이 테이블을 통해서만 연결됨
 * - fileType 으로 유저 파일 분리
 * - 프로필 이미지: 유저당 1개, 교체 시 기존 파일 즉시 삭제
 */
@Entity
@Table(name = "user_file", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_file_id", columnList = "file_id"),
        @Index(name = "idx_user_file_type", columnList = "user_id, file_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원 ID (외래키)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 파일 메타데이터 ID (외래키)
     */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /**
     * 파일 역할 구분
     * - PROFILE : 프로필 이미지 (유저당 1개)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private UserFileType fileType;

    /**
     * 파일 순서 (선택사항, 추후 확장용)
     * 본문 파일의 표시 순서를 지정할 때 사용
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder
    public UserFile(Long userId, Long fileId, UserFileType fileType, Integer displayOrder) {
        this.userId = userId;
        this.fileId = fileId;
        this.fileType = fileType;
        this.displayOrder = displayOrder;
    }
}