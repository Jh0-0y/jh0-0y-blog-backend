package com.blog.backend.infra.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;  // Access Token 유효시간 (ms)

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;  // Refresh Token 유효시간 (ms)

    private SecretKey key;

    /**
     * 초기화: Secret Key 생성
     * - Base64로 인코딩된 secret을 디코딩하여 Key 객체 생성
     * - HMAC-SHA256 알고리즘 사용
     */
    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     *
     * @param userId 사용자 ID (subject로 저장)
     * @param email 사용자 이메일 (claim으로 저장)
     * @return 생성된 Access Token
     */
    public String createAccessToken(Long userId, String email) {
        return createToken(userId, email, accessTokenValidity);
    }

    /**
     * Refresh Token 생성
     * - Access Token보다 유효기간이 김
     * - Access Token 재발급에 사용
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @return 생성된 Refresh Token
     */
    public String createRefreshToken(Long userId, String email) {
        return createToken(userId, email, refreshTokenValidity);
    }

    /**
     * 토큰 생성 (공통 로직)
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param validity 유효시간 (ms)
     * @return 생성된 JWT 토큰
     */
    private String createToken(Long userId, String email, long validity) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))  // 토큰 주체 (사용자 ID)
                .claim("email", email)            // 추가 정보
                .setIssuedAt(now)                    // 발급 시간
                .setExpiration(expiration)           // 만료 시간
                .signWith(key)                    // 서명
                .compact();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token 검증할 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 토큰 형식입니다: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("토큰 서명이 유효하지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 이메일 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * 토큰에서 Claims(페이로드) 파싱
     *
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Access Token 유효시간 반환 (ms)
     */
    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    /**
     * Refresh Token 유효시간 반환 (ms)
     */
    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }
}