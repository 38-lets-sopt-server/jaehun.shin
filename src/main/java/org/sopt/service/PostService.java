package org.sopt.service;

import java.util.ArrayList;
import java.util.List;
import org.sopt.domain.Post;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.PostNotFoundException;
import org.sopt.repository.PostRepository;

public class PostService {
    private final PostRepository postRepository = new PostRepository();

    // CREATE
    public CreatePostResponse createPost(CreatePostRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다!");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다!");
        }
        String createdAt = java.time.LocalDateTime.now().toString();
        Post post = new Post(
                postRepository.generateId(),
                request.getTitle(),
                request.getContent(),
                request.getAuthor(),
                createdAt
        );
        postRepository.save(post);
        return new CreatePostResponse(post.getId(), "게시글 등록 완료!");
    }

    // READ - 전체 📝 과제
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            responses.add(new PostResponse(post));
        }

        return responses;
    }

    // READ - 단건 📝 과제
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new PostNotFoundException(id);
        }

        return new PostResponse(post);
    }

    // UPDATE 📝 과제
    public void updatePost(Long id, String newTitle, String newContent) {
        Post post = postRepository.findById(id);

        if (post == null) {
            throw new PostNotFoundException(id);
        }

        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다!");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다!");
        }

        post.update(newTitle, newContent);
    }

    // DELETE 📝 과제
    public void deletePost(Long id) {
        Post post = postRepository.findById(id);

        if (post == null) {
            throw new PostNotFoundException(id);
        }

        postRepository.deleteById(id);
    }
}
