package org.sopt.service;

import java.util.ArrayList;
import java.util.List;

import org.sopt.domain.Post;
import org.sopt.domain.User;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.PostNotFoundException;
import org.sopt.exception.UserNotFoundException;
import org.sopt.repository.PostRepository;
import org.sopt.repository.UserRepository;
import org.sopt.validator.PostValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostValidator postValidator = new PostValidator();

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    @Transactional
    public CreatePostResponse createPost(CreatePostRequest request) {

        postValidator.validateCreate(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        Post post = new Post(
                request.getTitle(),
                request.getContent(),
                user
        );

        Post savedPost = postRepository.save(post);
        return new CreatePostResponse(savedPost.getId());
    }

    // READ - 전체 📝 과제
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            responses.add(new PostResponse(post));
        }

        return responses;
    }

    // READ - 단건 📝 과제
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFoundException::new);

        return new PostResponse(post);
    }

    // UPDATE 📝 과제
    @Transactional
    public void updatePost(Long id, UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFoundException::new);

        postValidator.validateUpdate(request);
        post.update(request.getTitle(), request.getContent());
    }

    // DELETE 📝 과제
    @Transactional
    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(PostNotFoundException::new);

        postRepository.deleteById(id);
    }
}
