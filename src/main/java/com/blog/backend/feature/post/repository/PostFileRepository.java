package com.blog.backend.feature.post.repository;

import com.blog.backend.feature.post.entity.PostFile;
import com.blog.backend.feature.post.entity.PostFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * PostFile 매핑 테이블 Repository
 */
public interface PostFileRepository extends JpaRepository<PostFile, Long> {

    /**
     * 특정 게시글의 모든 파일 매핑 조회
     *
     * @param postId 게시글 ID
     * @return 파일 매핑 목록
     */
    List<PostFile> findByPostId(Long postId);

    /**
     * 특정 게시글의 특정 타입 파일 매핑 조회
     *
     * @param postId 게시글 ID
     * @param fileType 파일 타입 (THUMBNAIL / CONTENT)
     * @return 파일 매핑 목록
     */
    List<PostFile> findByPostIdAndFileType(Long postId, PostFileType fileType);

    /**
     * 특정 게시글의 썸네일 매핑 조회 (단일)
     *
     * @param postId 게시글 ID
     * @param fileType THUMBNAIL
     * @return 썸네일 매핑 (Optional)
     */
    Optional<PostFile> findTopByPostIdAndFileType(Long postId, PostFileType fileType);

    /**
     * 특정 게시글의 본문 파일 ID 목록 조회
     *
     * @param postId 게시글 ID
     * @param fileType CONTENT
     * @return 본문 파일 ID 목록
     */
    @Query("SELECT pf.fileId FROM PostFile pf WHERE pf.postId = :postId AND pf.fileType = :fileType")
    List<Long> findFileIdsByPostIdAndFileType(@Param("postId") Long postId, @Param("fileType") PostFileType fileType);

    /**
     * 특정 게시글의 모든 파일 ID 조회
     *
     * @param postId 게시글 ID
     * @return 파일 ID 목록
     */
    @Query("SELECT pf.fileId FROM PostFile pf WHERE pf.postId = :postId")
    List<Long> findFileIdsByPostId(@Param("postId") Long postId);

    /**
     * 특정 파일이 어떤 게시글들에 연결되어 있는지 조회
     *
     * @param fileId 파일 ID
     * @return 게시글 ID 목록
     */
    @Query("SELECT pf.postId FROM PostFile pf WHERE pf.fileId = :fileId")
    List<Long> findPostIdsByFileId(@Param("fileId") Long fileId);

    /**
     * 특정 게시글의 모든 파일 매핑 삭제 (벌크 연산)
     *
     * @param postId 게시글 ID
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM PostFile pf WHERE pf.postId = :postId")
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 특정 게시글의 썸네일 매핑만 삭제 (벌크 연산)
     *
     * @param postId 게시글 ID
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM PostFile pf WHERE pf.postId = :postId AND pf.fileType = 'THUMBNAIL'")
    int deleteThumbnailByPostId(@Param("postId") Long postId);

    /**
     * 특정 게시글의 본문 파일 매핑만 삭제 (벌크 연산)
     *
     * @param postId 게시글 ID
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM PostFile pf WHERE pf.postId = :postId AND pf.fileType = 'CONTENT'")
    int deleteContentFilesByPostId(@Param("postId") Long postId);

    /**
     * 특정 파일의 모든 매핑 삭제 (벌크 연산)
     *
     * @param fileId 파일 ID
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM PostFile pf WHERE pf.fileId = :fileId")
    int deleteByFileId(@Param("fileId") Long fileId);

    /**
     * 여러 파일 ID에 대한 매핑 삭제 (벌크 연산)
     *
     * @param fileIds 파일 ID 목록
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM PostFile pf WHERE pf.fileId IN :fileIds")
    int deleteByFileIdIn(@Param("fileIds") List<Long> fileIds);

    /**
     * 특정 파일이 특정 게시글에 이미 연결되어 있는지 확인
     *
     * @param postId 게시글 ID
     * @param fileId 파일 ID
     * @return 존재 여부
     */
    boolean existsByPostIdAndFileId(Long postId, Long fileId);
}