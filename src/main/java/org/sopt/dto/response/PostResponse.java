package org.sopt.dto.response;

import org.sopt.domain.Post;

public class PostResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final long likeCount;

    public PostResponse(Post post) {
        this(post, 0);
    }

    public PostResponse(Post post, long likeCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getUser().getNickname();
        this.likeCount = likeCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public long getLikeCount() {
        return likeCount;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + title + "\n" + content;
    }
}
