package org.sopt.exception;

public class PostNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public PostNotFoundException(Long id) {
        super("ID가 " + id + "인 게시글을 찾을 수 없습니다.");
        this.errorCode = ErrorCode.POST_NOT_FOUND;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}



