package org.sopt.exception;

import org.sopt.dto.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleUserNotFound(UserNotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        return buildErrorResponse(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handlePostNotFound(PostNotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        return buildErrorResponse(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(PostValidationException.class)
    public ResponseEntity<BaseResponse<Void>> handlePostValidation(PostValidationException e) {
        ErrorCode errorCode = e.getErrorCode();
        return buildErrorResponse(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return buildErrorResponse(errorCode, e.getMessage());
    }

    private ResponseEntity<BaseResponse<Void>> buildErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.fail(errorCode.getCode(), message));
    }
}
