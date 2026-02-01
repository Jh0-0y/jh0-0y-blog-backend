package com.blog.backend.feature.stack.controller;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.dto.StackRequest;
import com.blog.backend.feature.stack.service.AdminStackService;
import com.blog.backend.global.core.response.ApiResponse;
import com.blog.backend.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 어드민 스택 컨트롤러
 *
 * 스택 생성, 수정, 삭제 (어드민 전용)
 */
@RestController
@RequestMapping("/api/admin/stacks")
@RequiredArgsConstructor
public class AdminStackController {

    private final AdminStackService adminStackService;

    /**
     * 스택 생성 (어드민 전용)
     * POST /api/admin/stacks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StackResponse.StackItem>> createStack(
            @Valid @RequestBody StackRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = adminStackService.createStack(request, userDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(stackItem, "스택이 생성되었습니다"));
    }

    /**
     * 스택 수정 (어드민 전용)
     * PUT /api/admin/stacks/{stackId}
     */
    @PutMapping("/{stackId}")
    public ResponseEntity<ApiResponse<StackResponse.StackItem>> updateStack(
            @PathVariable Long stackId,
            @Valid @RequestBody StackRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = adminStackService.updateStack(stackId, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(stackItem, "스택이 수정되었습니다"));
    }

    /**
     * 스택 삭제 (어드민 전용)
     * DELETE /api/admin/stacks/{stackId}
     */
    @DeleteMapping("/{stackId}")
    public ResponseEntity<ApiResponse<Void>> deleteStack(
            @PathVariable Long stackId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        adminStackService.deleteStack(stackId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "스택이 삭제되었습니다"));
    }
}