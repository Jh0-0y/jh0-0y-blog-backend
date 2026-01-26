package com.blog.backend.global.security.auth;

import com.blog.backend.feature.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails 구현체
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * 사용자 ID 반환
     * - Controller에서 인증된 사용자 ID 조회 시 사용
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 권한 목록 반환
     * - ROLE_ADMIN, ROLE_USER 등
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getKey()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자 식별자 반환
     * - Spring Security는 이 값으로 사용자를 식별
     * - 여기선 email 사용
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부
     * - true: 만료되지 않음
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부
     * - true: 잠기지 않음
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부
     * - true: 만료되지 않음
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     * - true: 활성화됨
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}