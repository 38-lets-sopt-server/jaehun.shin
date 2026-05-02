package org.sopt.dto.response;

public class BaseResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public BaseResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, "SUCCESS", message, data);
    }

    public static <T> BaseResponse<T> fail(String code, String message) {
        return new BaseResponse<>(false, code, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

}
