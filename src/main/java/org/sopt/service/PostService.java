package org.sopt.service;

import java.util.ArrayList;
import java.util.List;
import org.sopt.domain.Post;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.PostNotFoundException;
import org.sopt.repository.PostRepository;
import org.sopt.validator.PostValidator;

public class PostService {
    private final PostRepository postRepository = new PostRepository();
    private final PostValidator postValidator = new PostValidator();
    // CREATE
    public CreatePostResponse createPost(CreatePostRequest request) {

        postValidator.validateCreate(request);

        String createdAt = java.time.LocalDateTime.now().toString();
        Post post = new Post(
                postRepository.generateId(),
                request.getTitle(),
                request.getContent(),
                request.getAuthor(),
                createdAt
        );
        postRepository.save(post);
        return new CreatePostResponse(post.getId());
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
    public void updatePost(Long id, UpdatePostRequest request) {
        Post post = postRepository.findById(id);

        if (post == null) {
            throw new PostNotFoundException(id);
        }

        postValidator.validateUpdate(request);
        post.update(request.getTitle(), request.getContent());
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
