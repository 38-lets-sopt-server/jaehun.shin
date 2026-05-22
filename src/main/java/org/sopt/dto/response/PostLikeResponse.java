package org.sopt.dto.response;

public class PostLikeResponse {

    private final Long postId;
    private final long likeCount;
    private final boolean likedByMe;

    public PostLikeResponse(Long postId, long likeCount, boolean likedByMe) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
    }

    public Long getPostId() {
        return postId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByMe() {
        return likedByMe;
    }
}
