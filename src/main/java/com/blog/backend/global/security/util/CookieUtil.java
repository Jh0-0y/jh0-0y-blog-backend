package com.blog.backend.global.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Value("${app.cookie.secure:false}")
    private boolean isSecure;

    @Value("${app.cookie.sameSite:Lax}")
    private String sameSite;

    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";
    private static final String REFRESH_TOKEN_PATH = "/api/auth";

    /**
     * Access Token 쿠키 생성
     */
    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(isSecure)
                .path(ACCESS_TOKEN_PATH)
                .maxAge(accessTokenValidity / 1000)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Refresh Token 쿠키 생성
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(isSecure)
                .path(REFRESH_TOKEN_PATH)
                .maxAge(refreshTokenValidity / 1000)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 토큰 쿠키 삭제 (로그아웃)
     */
    public void deleteTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(isSecure)
                .path(ACCESS_TOKEN_PATH)
                .maxAge(0)
                .sameSite(sameSite)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(isSecure)
                .path(REFRESH_TOKEN_PATH)
                .maxAge(0)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    /**
     * 쿠키에서 Access Token 추출
     */
    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_NAME);
    }

    /**
     * 쿠키에서 Refresh Token 추출
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_NAME);
    }

    /**
     * 쿠키 값 추출
     */
    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}