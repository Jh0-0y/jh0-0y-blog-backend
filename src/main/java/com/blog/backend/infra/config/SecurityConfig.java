package com.blog.backend.infra.config;

import com.blog.backend.infra.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security Filter Chain 설정
     * - 모든 HTTP 요청에 대한 보안 규칙 정의
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API는 stateless하므로 CSRF 공격에 안전)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 사용 안 함 (JWT 기반 인증 사용 예정)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers("/api/auth/**").permitAll()           // 로그인, 회원가입
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()  // 게시글 조회
                        .requestMatchers(HttpMethod.GET, "/api/tags/**").permitAll()   // 태그 조회
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                // UsernamePasswordAuthenticationFilter: 폼 로그인 처리 필터
                // JWT 필터가 먼저 실행되어 토큰 기반 인증 처리
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * CORS 설정
     * - Cross-Origin Resource Sharing
     * - 프론트엔드(다른 도메인)에서 API 호출 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (프론트엔드 주소)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",    // Vite 개발 서버
                "http://localhost:3000"     // 기타 개발 서버
        ));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));
        // 인증 정보(쿠키, Authorization 헤더) 허용
        configuration.setAllowCredentials(true);
        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * AuthenticationManager 빈 등록
     * - 로그인 시 인증 처리에 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
