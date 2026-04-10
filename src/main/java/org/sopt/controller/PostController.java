package org.sopt.controller;

import java.util.List;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.CommonResponse;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.PostNotFoundException;
import org.sopt.service.PostService;

public class PostController {
    private final PostService postService = new PostService();

    // POST /posts
//    public CreatePostResponse createPost(CreatePostRequest request) {
//        try {
//            return postService.createPost(request);
//        } catch (IllegalArgumentException e) {
//            return new CreatePostResponse(null, "🚫 " + e.getMessage());
//        }
//    }

    public CommonResponse<CreatePostResponse> createPost(CreatePostRequest request) {
        try {
            CreatePostResponse response = postService.createPost(request);
            return CommonResponse.success("게시글 등록 완료", response);
        } catch (IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // GET /posts 📝 과제
//    public List<PostResponse> getAllPosts() {
//        return postService.getAllPosts();
//    }

    public CommonResponse<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return CommonResponse.success("게시글 전체 목록 조회 성공", posts);
    }

    // GET /posts/{id} 📝 과제
//    public PostResponse getPost(Long id) {
//        try {
//            return postService.getPost(id);
//        } catch (PostNotFoundException | IllegalArgumentException e) {
//            System.out.println(e.getMessage());
//            return null;
//        }
//    }

    public CommonResponse<PostResponse> getPost(Long id) {
        try {
            PostResponse post = postService.getPost(id);
            return CommonResponse.success("게시글 조회 성공", post);
        } catch (PostNotFoundException | IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // PUT /posts/{id} 📝 과제
//    public void updatePost(Long id, UpdatePostRequest request) {
//        try {
//            postService.updatePost(id, request);
//        } catch (PostNotFoundException | IllegalArgumentException e) {
//            System.out.println(e.getMessage());
//        }
//    }

    public CommonResponse<Void> updatePost(Long id, UpdatePostRequest request) {
        try {
            postService.updatePost(id, request);
            return CommonResponse.success("게시글 수정 완료", null);
        } catch (PostNotFoundException | IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // DELETE /posts/{id} 📝 과제
//    public void deletePost(Long id) {
//        try {
//            postService.deletePost(id);
//        } catch (PostNotFoundException e) {
//            System.out.println(e.getMessage());
//        }
//    }

    public CommonResponse<Void> deletePost(Long id) {
        try {
            postService.deletePost(id);
            return CommonResponse.success("게시글 삭제완료", null);
        } catch (PostNotFoundException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }
}
