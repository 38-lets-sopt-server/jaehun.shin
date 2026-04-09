package org.sopt.dto.request;

public class UpdatePostRequest {
    private String title;
    private String content;
    private String author;

    public UpdatePostRequest() {
    }

    public UpdatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
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
}
