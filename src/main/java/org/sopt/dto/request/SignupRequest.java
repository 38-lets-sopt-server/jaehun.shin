package org.sopt.dto.request;

public class SignupRequest {

    private String nickname;
    private String email;
    private String password;

    public SignupRequest() {
    }

    public SignupRequest(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
