package org.sopt.service;

import java.util.ArrayList;
import java.util.List;

import org.sopt.domain.Post;
import org.sopt.domain.User;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostLikeResponse;
import org.sopt.dto.response.PostResponse;
import org.sopt.exception.AuthorizationException;
import org.sopt.exception.PostNotFoundException;
import org.sopt.exception.UserNotFoundException;
import org.sopt.domain.PostLike;
import org.sopt.repository.PostLikeRepository;
import org.sopt.repository.PostRepository;
import org.sopt.repository.UserRepository;
import org.sopt.validator.PostValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostValidator postValidator = new PostValidator();

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            PostLikeRepository postLikeRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // CREATE
    @Transactional
    public CreatePostResponse createPost(CreatePostRequest request, Long memberId) {

        postValidator.validateCreate(request);

        User user = userRepository.findById(memberId)
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
            responses.add(new PostResponse(post, postLikeRepository.countByPostId(post.getId())));
        }

        return responses;
    }

    // READ - 단건 📝 과제
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        return new PostResponse(post, postLikeRepository.countByPostId(post.getId()));
    }

    // UPDATE 📝 과제
    @Transactional
    public void updatePost(Long id, UpdatePostRequest request, Long memberId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        validateOwner(post, memberId);

        postValidator.validateUpdate(request);
        post.update(request.getTitle(), request.getContent());
    }

    // DELETE 📝 과제
    @Transactional
    public void deletePost(Long id, Long memberId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        validateOwner(post, memberId);

        postLikeRepository.deleteByPostId(id);
        postRepository.delete(post);
    }

    @Transactional
    public PostLikeResponse likePost(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        User user = userRepository.findById(memberId)
                .orElseThrow(UserNotFoundException::new);

        if (!postLikeRepository.existsByPostIdAndUserId(postId, memberId)) {
            postLikeRepository.save(new PostLike(post, user));
        }

        return new PostLikeResponse(postId, postLikeRepository.countByPostId(postId), true);
    }

    @Transactional
    public PostLikeResponse unlikePost(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (postLikeRepository.existsByPostIdAndUserId(postId, memberId)) {
            postLikeRepository.deleteByPostIdAndUserId(postId, memberId);
        }

        return new PostLikeResponse(post.getId(), postLikeRepository.countByPostId(postId), false);
    }

    private void validateOwner(Post post, Long memberId) {
        if (!post.getUser().getId().equals(memberId)) {
            throw new AuthorizationException();
        }
    }
}
