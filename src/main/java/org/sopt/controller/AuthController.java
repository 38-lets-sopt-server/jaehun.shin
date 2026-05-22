package org.sopt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.dto.request.SignupRequest;
import org.sopt.dto.response.AuthenticatedMemberResponse;
import org.sopt.dto.response.BaseResponse;
import org.sopt.dto.response.TokenResponse;
import org.sopt.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입 (BCrypt 비밀번호 암호화 저장)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "회원가입 실패")
    })
    @PostMapping("/auth/signup")
    public ResponseEntity<BaseResponse<AuthenticatedMemberResponse>> signup(
            @RequestBody SignupRequest request
    ) {
        AuthenticatedMemberResponse member = authService.signup(request);
        return ResponseEntity.ok(BaseResponse.success("회원가입 성공", member));
    }

    @Operation(summary = "로그인 (Access Token + Refresh Token 발급)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "로그인 실패")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) {
        TokenResponse tokens = authService.login(email, password);
        return ResponseEntity.ok(BaseResponse.success("로그인 성공", tokens));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/auth/reissue")
    public ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String refreshToken = extractBearerToken(authorization);
        TokenResponse tokens = authService.reissue(refreshToken);
        return ResponseEntity.ok(BaseResponse.success("토큰 재발급 성공", tokens));
    }

    @Operation(summary = "로그아웃 (Refresh Token 삭제 + Access Token 블랙리스트)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/auth/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            Authentication authentication
    ) {
        String accessToken = extractBearerToken(authorization);
        authService.logout(getMemberId(authentication), accessToken);
        return ResponseEntity.ok(BaseResponse.success("로그아웃 성공", null));
    }

    @Operation(summary = "내 정보 조회 (Access Token 검증)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<AuthenticatedMemberResponse>> me(Authentication authentication) {
        Long memberId = getMemberId(authentication);
        AuthenticatedMemberResponse member = authService.getMemberById(memberId);

        return ResponseEntity.ok(BaseResponse.success("내 정보 조회 성공", member));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Bearer 토큰이 아닙니다.");
        }

        return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    private Long getMemberId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("인증되지 않았습니다.");
        }

        return Long.parseLong(authentication.getName());
    }
}
