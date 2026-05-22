package org.sopt.service;

import org.sopt.domain.AccessTokenBlacklist;
import org.sopt.domain.RefreshToken;
import org.sopt.domain.SocialAccount;
import org.sopt.domain.SocialProvider;
import org.sopt.domain.User;
import org.sopt.dto.oauth.OAuthUserInfo;
import org.sopt.dto.request.OAuthLoginRequest;
import org.sopt.dto.request.SignupRequest;
import org.sopt.dto.response.AuthenticatedMemberResponse;
import org.sopt.dto.response.TokenResponse;
import org.sopt.exception.AuthorizationException;
import org.sopt.repository.AccessTokenBlacklistRepository;
import org.sopt.repository.RefreshTokenRepository;
import org.sopt.repository.SocialAccountRepository;
import org.sopt.repository.UserRepository;
import org.sopt.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final OAuthUserInfoClient oAuthUserInfoClient;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.jwt.refresh-token-expires-in-seconds:1209600}")
    private long refreshTokenExpiresInSeconds;

    public AuthService(
            RefreshTokenRepository refreshTokenRepository,
            AccessTokenBlacklistRepository accessTokenBlacklistRepository,
            SocialAccountRepository socialAccountRepository,
            UserRepository userRepository,
            JwtService jwtService,
            OAuthUserInfoClient oAuthUserInfoClient,
            PasswordEncoder passwordEncoder
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenBlacklistRepository = accessTokenBlacklistRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.oAuthUserInfoClient = oAuthUserInfoClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthenticatedMemberResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getNickname(), request.getEmail(), encodedPassword);
        User savedUser = userRepository.save(user);

        return AuthenticatedMemberResponse.from(savedUser);
    }

    @Transactional
    public TokenResponse login(String email, String password) {
        User user = loginWithCredentials(email, password);
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse oauthLogin(SocialProvider provider, OAuthLoginRequest request) {
        validateOAuthLoginRequest(request);

        OAuthUserInfo userInfo = oAuthUserInfoClient.getUserInfo(provider, request.getAccessToken());
        User user = socialAccountRepository
                .findByProviderAndProviderUserId(provider, userInfo.getProviderUserId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> connectSocialAccount(provider, userInfo));

        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        refreshTokenRepository.deleteByMemberId(user.getId());
        refreshTokenRepository.save(RefreshToken.of(user.getId(), refreshToken, refreshTokenExpiresInSeconds));

        return TokenResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        Long memberId = jwtService.verifyAndGetMemberId(refreshToken);
        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token을 찾을 수 없습니다."));

        if (savedRefreshToken.isExpired()) {
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다.");
        }

        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        String newAccessToken = jwtService.generateAccessToken(memberId, user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(memberId);

        savedRefreshToken.rotate(newRefreshToken, refreshTokenExpiresInSeconds);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long memberId, String accessToken) {
        Long tokenMemberId = jwtService.verifyAndGetMemberId(accessToken);
        if (!tokenMemberId.equals(memberId)) {
            throw new AuthorizationException();
        }

        refreshTokenRepository.deleteByMemberId(memberId);

        if (!accessTokenBlacklistRepository.existsByToken(accessToken)) {
            accessTokenBlacklistRepository.save(AccessTokenBlacklist.of(
                    memberId,
                    accessToken,
                    jwtService.verifyAndGetExpiresAt(accessToken)
            ));
        }
    }

    @Transactional(readOnly = true)
    public AuthenticatedMemberResponse getMemberById(Long memberId) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        return AuthenticatedMemberResponse.from(user);
    }

    private User loginWithCredentials(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return user;
    }

    private User connectSocialAccount(SocialProvider provider, OAuthUserInfo userInfo) {
        User user = findUserByEmail(userInfo.getEmail())
                .orElseGet(() -> userRepository.save(new User(userInfo.getNickname(), userInfo.getEmail())));

        socialAccountRepository.save(new SocialAccount(provider, userInfo.getProviderUserId(), user));
        return user;
    }

    private Optional<User> findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email);
    }

    private void validateOAuthLoginRequest(OAuthLoginRequest request) {
        if (request == null || request.getAccessToken() == null || request.getAccessToken().isBlank()) {
            throw new IllegalArgumentException("소셜 로그인 Access Token은 필수입니다.");
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원가입 요청이 비어있습니다.");
        }

        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
    }
}
