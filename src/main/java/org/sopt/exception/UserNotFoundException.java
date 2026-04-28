package org.sopt.exception;

public class UserNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.USER_NOT_FOUND;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
