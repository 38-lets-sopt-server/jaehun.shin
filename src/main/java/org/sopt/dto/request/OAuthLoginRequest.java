package org.sopt.dto.request;

public class OAuthLoginRequest {

    private String accessToken;

    public OAuthLoginRequest() {
    }

    public OAuthLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
