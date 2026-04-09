package org.sopt.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("ID 가 " + id + " 인 게시글이 없습니다.");
    }
}

