package com.blog.backend.feature.post.service;

import com.blog.backend.feature.file.service.FileMetadataService;
import com.blog.backend.feature.post.dto.PostRequest;
import com.blog.backend.feature.post.dto.PostResponse;
import com.blog.backend.feature.post.dto.PostSearchCondition;
import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostFile;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.repository.PostFileRepository;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.repository.PostSpecification;
import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.repository.StackRepository;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StackRepository stackRepository;
    private final PostFileRepository postFileRepository;
    private final FileMetadataService fileMetadataService;

    // ========== CRUD ========== //

    @Override
    @Transactional
    public PostResponse.Detail createPost(Long userId, PostRequest.Create request) {
        User user = findUserById(userId);
        existsByTitle(request.getTitle());

        Post post = Post.builder()
                .user(user)
                .postType(request.getPostType())
                .title(request.getTitle())
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.PRIVATE)
                .build();

        // 자유 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            post.updateTags(request.getTags());
        }

        // 기술 스택 처리
        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        Post savedPost = postRepository.save(post);

        // ===== 파일 매핑 생성 (중간 테이블) ===== //
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            log.info("게시글 생성 - 파일 매핑 시작: postId={}, fileCount={}",
                    savedPost.getId(), request.getFileIds().size());

            // 파일 존재 여부 검증
            fileMetadataService.validateFilesExist(request.getFileIds());

            // PostFile 매핑 생성
            List<PostFile> postFiles = request.getFileIds().stream()
                    .map(fileId -> PostFile.of(savedPost.getId(), fileId))
                    .collect(Collectors.toList());

            postFileRepository.saveAll(postFiles);

            log.info("게시글 생성 - 파일 매핑 완료: postId={}, mappedCount={}",
                    savedPost.getId(), postFiles.size());
        }
        // ===== 파일 매핑 완료 ===== //

        return PostResponse.Detail.from(savedPost, null, null);
    }

    @Override
    @Transactional
    public PostResponse.Detail updatePost(Long userId, Long postId, PostRequest.Update request) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        // 제목이 변경될 경우에만 중복 체크 (자기 자신 제외)
        if (!post.getTitle().equals(request.getTitle())) {
            existsByTitle(request.getTitle());
        }

        post.update(
                request.getPostType(),
                request.getTitle(),
                request.getExcerpt(),
                request.getContent(),
                request.getStatus()
        );

        // 자유 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            post.updateTags(request.getTags());
        }

        // 기술 스택 처리
        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        // ===== 파일 매핑 처리 ===== //

        // 1. 삭제할 파일이 있으면 매핑 제거
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            log.info("게시글 수정 - 파일 매핑 삭제 시작: postId={}, deleteCount={}",
                    postId, request.getDeletedFileIds().size());

            // 기존 매핑에서 삭제 대상만 필터링
            List<PostFile> existingMappings = postFileRepository.findByPostId(postId);
            List<Long> toDelete = existingMappings.stream()
                    .filter(pf -> request.getDeletedFileIds().contains(pf.getFileId()))
                    .map(PostFile::getId)
                    .collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                postFileRepository.deleteAllById(toDelete);
                log.info("게시글 수정 - 파일 매핑 삭제 완료: postId={}, deletedCount={}",
                        postId, toDelete.size());
            }
        }

        // 2. 새로 추가할 파일이 있으면 매핑 생성
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            log.info("게시글 수정 - 새 파일 매핑 시작: postId={}, newFileCount={}",
                    postId, request.getFileIds().size());

            // 파일 존재 여부 검증
            fileMetadataService.validateFilesExist(request.getFileIds());

            // 중복 매핑 방지: 이미 연결된 파일 제외
            List<Long> existingFileIds = postFileRepository.findFileIdsByPostId(postId);
            List<Long> newFileIds = request.getFileIds().stream()
                    .filter(fileId -> !existingFileIds.contains(fileId))
                    .collect(Collectors.toList());

            if (!newFileIds.isEmpty()) {
                List<PostFile> newMappings = newFileIds.stream()
                        .map(fileId -> PostFile.of(postId, fileId))
                        .collect(Collectors.toList());

                postFileRepository.saveAll(newMappings);

                log.info("게시글 수정 - 새 파일 매핑 완료: postId={}, mappedCount={}",
                        postId, newMappings.size());
            } else {
                log.info("게시글 수정 - 모든 파일이 이미 매핑되어 있음: postId={}", postId);
            }
        }
        // ===== 파일 매핑 처리 완료 ===== //

        return PostResponse.Detail.from(post, null, null);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostById(postId);
        validateAuthor(post, userId);

        // ===== 파일 매핑 삭제 (벌크 연산) ===== //
        log.info("게시글 삭제 - 파일 매핑 삭제 시작: postId={}", postId);

        int deletedMappings = postFileRepository.deleteByPostId(postId);

        log.info("게시글 삭제 - 파일 매핑 삭제 완료: postId={}, deletedMappings={}", postId, deletedMappings);

        // 주의: 실제 파일(FileMetadata)은 삭제하지 않음
        // 고아 파일이 된 파일들은 스케줄러가 24시간 후 자동 삭제
        // ===== 파일 매핑 삭제 완료 ===== //

        post.clearStack();
        postRepository.delete(post);

        log.info("게시글 삭제 완료: postId={}", postId);
    }


    @Override
    public PostResponse.Detail getPost(Long postId) {
        Post post = postRepository.findByIdWithStacks(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        Post prevPost = postRepository.findPreviousPost(postId, PostStatus.PUBLIC).orElse(null);
        Post nextPost = postRepository.findNextPost(postId, PostStatus.PUBLIC).orElse(null);

        return PostResponse.Detail.from(post, prevPost, nextPost);
    }

    // ========== 복합 필터링 (신규) ========== //
    @Override
    public Page<PostResponse.PostItems> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withCondition(condition), pageable)
                .map(PostResponse.PostItems::from);
    }

    @Override
    public Page<PostResponse.PostItems> searchMyPosts(Long userId, PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withUserAndCondition(userId, condition), pageable)
                .map(PostResponse.PostItems::from);
    }

    // ========== 기존 메서드 (하위 호환) ========== //
    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getPublicPosts(Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getPostsByPostType(PostType postType, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(postType, null, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getPostsByTag(String tagName, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, tagName, null);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> searchPosts(String keyword, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofPublic(null, null, keyword);
        return searchPosts(condition, pageable);
    }

    @Override
    @Deprecated
    public Page<PostResponse.PostItems> getMyPosts(Long userId, Pageable pageable) {
        PostSearchCondition condition = PostSearchCondition.ofMine(null, null, null);
        return searchMyPosts(userId, condition, pageable);
    }

    // ========== Private Methods ========== //

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    private void existsByTitle(String title) {
        if (postRepository.existsByTitle(title)) {
            throw CustomException.conflict("이미 존재하는 제목입니다.");
        }
    }

    private void validateAuthor(Post post, Long userId) {
        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("게시글 수정/삭제 권한이 없습니다");
        }
    }
}