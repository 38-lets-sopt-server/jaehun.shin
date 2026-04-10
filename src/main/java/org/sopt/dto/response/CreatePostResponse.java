package org.sopt.dto.response;

public class CreatePostResponse {
    private final Long id;

    public CreatePostResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
