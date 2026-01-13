//package com.blog.backend.global.image.controller;
//
//
//import com.blog.backend.global.common.ApiResponse;
//import com.blog.backend.global.image.dto.ImageResponse;
//import com.blog.backend.global.image.service.ImageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/images")
//@RequiredArgsConstructor
//public class ImageController {
//
//    private final ImageService imageService;
//
//    /**
//     * 이미지 업로드
//     * POST /api/images
//     */
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<ImageResponse>> uploadImage(
//            @RequestParam("file") MultipartFile file
//    ) {
//        ImageResponse response = imageService.upload(file);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//}