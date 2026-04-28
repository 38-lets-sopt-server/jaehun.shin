package org.sopt.dto.response;

import org.sopt.domain.Post;

public class PostResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String author;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getUser().getNickname();
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

    @Override
    public String toString() {
        return "[" + id + "] " + title + "\n" + content;
    }
}
