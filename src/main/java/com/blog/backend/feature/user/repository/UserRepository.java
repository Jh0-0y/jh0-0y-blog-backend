package com.blog.backend.feature.user.repository;

import com.blog.backend.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * - 로그인 시 사용
     *
     * @param email 이메일
     * @return 사용자 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     *
     * @param nickname
     * @return 사용자 (Optional)
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 이메일 존재 여부 확인
     * - 회원가입 시 중복 검사
     *
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     * - 회원가입/수정 시 중복 검사
     *
     * @param nickname 닉네임
     * @return 존재 여부
     */
    boolean existsByNickname(String nickname);
}