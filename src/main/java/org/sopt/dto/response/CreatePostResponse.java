package org.sopt.dto.response;

public class CreatePostResponse {
    private final Long id;
    private final String message;

    public CreatePostResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
