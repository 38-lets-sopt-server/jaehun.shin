package org.sopt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sopt.domain.SocialProvider;
import org.sopt.dto.oauth.OAuthUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OAuthUserInfoClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String kakaoUserInfoUri;
    private final String googleUserInfoUri;

    public OAuthUserInfoClient(
            ObjectMapper objectMapper,
            @Value("${app.oauth.kakao.user-info-uri}") String kakaoUserInfoUri,
            @Value("${app.oauth.google.user-info-uri}") String googleUserInfoUri
    ) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
        this.kakaoUserInfoUri = kakaoUserInfoUri;
        this.googleUserInfoUri = googleUserInfoUri;
    }

    public OAuthUserInfo getUserInfo(SocialProvider provider, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("소셜 로그인 Access Token은 필수입니다.");
        }

        return switch (provider) {
            case KAKAO -> getKakaoUserInfo(accessToken);
            case GOOGLE -> getGoogleUserInfo(accessToken);
        };
    }

    private OAuthUserInfo getKakaoUserInfo(String accessToken) {
        JsonNode root = requestUserInfo(kakaoUserInfoUri, accessToken);
        String providerUserId = textOrNull(root.path("id"));
        JsonNode kakaoAccount = root.path("kakao_account");
        String email = textOrNull(kakaoAccount.path("email"));
        String nickname = textOrNull(kakaoAccount.path("profile").path("nickname"));

        validateProviderUserId(providerUserId);
        return new OAuthUserInfo(providerUserId, email, defaultNickname(nickname, "kakao", providerUserId));
    }

    private OAuthUserInfo getGoogleUserInfo(String accessToken) {
        JsonNode root = requestUserInfo(googleUserInfoUri, accessToken);
        String providerUserId = textOrNull(root.path("sub"));
        String email = textOrNull(root.path("email"));
        String nickname = textOrNull(root.path("name"));

        validateProviderUserId(providerUserId);
        return new OAuthUserInfo(providerUserId, email, defaultNickname(nickname, "google", providerUserId));
    }

    private JsonNode requestUserInfo(String uri, String accessToken) {
        try {
            String response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(response);
        } catch (RestClientException e) {
            throw new IllegalArgumentException("외부 인증 서버 사용자 정보 조회에 실패했습니다.");
        } catch (Exception e) {
            throw new IllegalArgumentException("소셜 로그인 응답을 해석할 수 없습니다.");
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private void validateProviderUserId(String providerUserId) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("소셜 로그인 사용자 식별값을 찾을 수 없습니다.");
        }
    }

    private String defaultNickname(String nickname, String providerName, String providerUserId) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }

        return providerName + "_" + providerUserId;
    }
}
