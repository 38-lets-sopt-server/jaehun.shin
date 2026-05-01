package org.sopt.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.BaseResponse;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "게시글 관련 API")
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "게시글 생성",
            description = "제목, 내용, 작성자 ID를 받아 게시글을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 제목 또는 내용이 비어있거나 길이 제한을 초과한 경우"),
            @ApiResponse(responseCode = "404", description = "작성자를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<CreatePostResponse>> createPost(@RequestBody CreatePostRequest request) {
        CreatePostResponse response = postService.createPost(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("게시글 등록 완료", response));
    }

    @Operation(
            summary = "게시글 전체 조회",
            description = "등록된 게시글 전체 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 전체 조회 성공")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<PostResponse>>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 전체 목록 조회 성공", posts));
    }

    @Operation(
            summary = "게시글 단건 조회",
            description = "게시글 ID로 특정 게시글을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PostResponse>> getPost(
            @Parameter(description = "조회할 게시글 ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        PostResponse post = postService.getPost(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 조회 성공", post));
    }

    @Operation(
            summary = "게시글 수정",
            description = "게시글 ID로 특정 게시글의 제목과 내용을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 제목 또는 내용이 비어있거나 길이 제한을 초과한 경우"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updatePost(
            @Parameter(description = "수정할 게시글 ID", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
    ) {
        postService.updatePost(id, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 수정 성공", null));
    }

    @Operation(
            summary = "게시글 삭제",
            description = "게시글 ID로 특정 게시글을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @Parameter(description = "삭제할 게시글 ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        postService.deletePost(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.success("게시글 삭제완료", null));
    }
}
