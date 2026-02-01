package com.blog.backend.feature.user.repository;

import com.blog.backend.feature.user.entity.UserFile;
import com.blog.backend.feature.user.entity.UserFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * UserFile(사용자-파일 매핑) Repository
 */
public interface UserFileRepository extends JpaRepository<UserFile, Long> {

    /**
     * 특정 사용자의 프로필 이미지 매핑 정보 조회
     *
     * @param userId 사용자 ID
     * @param fileType 파일 타입 (PROFILE)
     * @return UserFile 매핑 정보 (Optional)
     */
    Optional<UserFile> findByUserIdAndFileType(Long userId, UserFileType fileType);

    /**
     * 특정 사용자의 특정 파일 타입 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param fileType 파일 타입
     * @return 존재 여부
     */
    boolean existsByUserIdAndFileType(Long userId, UserFileType fileType);

    /**
     * 특정 사용자의 프로필 이미지 매핑 삭제
     *
     * @param userId 사용자 ID
     * @param fileType 파일 타입 (PROFILE)
     */
    void deleteByUserIdAndFileType(Long userId, UserFileType fileType);

    /**
     * 사용 중인 모든 파일 ID 조회 (스케줄러용)
     *
     * @return 파일 ID 목록
     */
    @Query("SELECT uf.fileId FROM UserFile uf")
    List<Long> findAllUsedFileIds();
}