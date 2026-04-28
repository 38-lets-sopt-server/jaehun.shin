package org.sopt.exception;

public class PostValidationException extends RuntimeException {
    private final ErrorCode errorCode;

    public PostValidationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

