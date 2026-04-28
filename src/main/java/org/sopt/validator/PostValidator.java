package org.sopt.validator;

import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.exception.ErrorCode;
import org.sopt.exception.PostValidationException;

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
            throw new PostValidationException(ErrorCode.POST_TITLE_REQUIRED);
        }

        if (title.length() > 50) {
            throw new PostValidationException(ErrorCode.POST_TITLE_TOO_LONG);
        }
    }

    public void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new PostValidationException(ErrorCode.POST_CONTENT_REQUIRED);
        }
    }
}
