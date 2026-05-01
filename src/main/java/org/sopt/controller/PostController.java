package org.sopt.controller;

import java.util.List;

import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.BaseResponse;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<CreatePostResponse>> createPost(@RequestBody CreatePostRequest request) {
        CreatePostResponse response = postService.createPost(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("게시글 등록 완료", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<PostResponse>>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 전체 목록 조회 성공", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PostResponse>> getPost(@PathVariable Long id) {
        PostResponse post = postService.getPost(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 조회 성공", post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
    ) {
        postService.updatePost(id, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 수정 성공", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 삭제완료", null));
    }
}
