package org.sopt.dto.oauth;

public class OAuthUserInfo {

    private final String providerUserId;
    private final String email;
    private final String nickname;

    public OAuthUserInfo(String providerUserId, String email, String nickname) {
        this.providerUserId = providerUserId;
        this.email = email;
        this.nickname = nickname;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }
}
