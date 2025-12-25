package com.portfolio.backend.domain.post;

import com.portfolio.backend.domain.post.dto.PostDetailResponse;
import com.portfolio.backend.domain.post.dto.PostRequest;
import com.portfolio.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class PostAdminController {
    
    private final PostService postService;
    
    // 글 상세 조회 (비공개 포함)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long id) {
        PostDetailResponse post = postService.getPost(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    // 글 생성
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody PostRequest request) {
        // TODO: 실제로는 인증된 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시로 1번 사용자
        
        PostDetailResponse post = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(post, "글이 작성되었습니다"));
    }
    
    // 글 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        PostDetailResponse post = postService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponse.success(post, "글이 수정되었습니다"));
    }
    
    // 글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "글이 삭제되었습니다"));
    }
}
