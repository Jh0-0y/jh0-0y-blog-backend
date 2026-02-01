package com.blog.backend.feature.user.controller;

import com.blog.backend.feature.user.dto.UserResponse;
import com.blog.backend.feature.user.service.UserService;
import com.blog.backend.global.core.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class BlogInfoController {

    private final UserService userService;

    /**
     * 블로그 사용자 정보
     * GET /api/user/{nickname}
     */
    @GetMapping("/{nickname}")
    public ResponseEntity<ApiResponse<UserResponse.BlogUserInfo>> signUp(
            @PathVariable String nickname
    ) {
        UserResponse.BlogUserInfo blogUser = userService.getBlogUser(nickname);
        return ResponseEntity.ok(ApiResponse.success(blogUser, "회원가입이 성공적으로 완료되었습니다."));
    }
}
