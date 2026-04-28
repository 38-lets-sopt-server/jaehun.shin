package org.sopt.dto.request;

public class CreatePostRequest {
    private String title;
    private String content;
    private Long userId;

    public CreatePostRequest() {
    }

    public CreatePostRequest(String title, String content, Long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getUserId() { return userId; }
}
