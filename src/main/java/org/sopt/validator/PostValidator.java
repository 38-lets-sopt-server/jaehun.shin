package org.sopt.validator;

import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;

public class PostValidator {

    public void validateCreate(CreatePostRequest request) {
        validateTitle(request.getTitle());
        validateContent(request.getContent());
    }

    public void validateUpdate(UpdatePostRequest request) {
        validateTitle(request.getTitle());
        validateContent(request.getContent());
    }

    public void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
    }

    public void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }
}
