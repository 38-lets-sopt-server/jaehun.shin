# 6차 세미나 Security 실습 정리

## 개요

이번 실습에서는 노션 `6TH SEMINAR | Security` 흐름에 맞춰 Spring Security와 JWT 인증 구조를 추가했다.

핵심 목표는 다음과 같다.

- `userId`를 헤더에 직접 넣어 요청 주체를 믿는 방식을 피한다.
- 로그인 시 Access Token과 Refresh Token을 발급한다.
- Refresh Token은 DB에 저장하고, 재발급 시 rotate한다.
- 인증이 필요한 API는 JWT Filter에서 먼저 토큰을 검증한다.
- 컨트롤러에서는 `Authentication` 객체를 통해 인증된 사용자 정보를 꺼낸다.

## 추가한 의존성

`build.gradle`에 Spring Security와 Auth0 Java JWT 의존성을 추가했다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'com.auth0:java-jwt:4.5.2'
```

노션 실습 코드가 `JWT.create()`, `JWT.require()`, `Algorithm.HMAC256(...)`, `DecodedJWT`를 사용하므로 Auth0 `java-jwt` 라이브러리를 사용했다.

## 환경변수

JWT 설정은 환경변수로 분리했다. 로그인 이메일과 비밀번호는 DB의 `users` 테이블에 저장된 사용자 정보를 기준으로 검증한다.

```bash
JWT_SECRET=assignment-security-practice-jwt-secret-key-2026
JWT_ACCESS_TOKEN_EXPIRES_IN_SECONDS=1800
JWT_REFRESH_TOKEN_EXPIRES_IN_SECONDS=1209600
```

## 로그인 흐름

`AuthService`는 노션 실습의 `MemberRepository` 역할을 현재 프로젝트의 `UserRepository`로 대체해 사용한다.

1. `email`로 사용자를 조회한다.
2. DB에 저장된 `password`와 요청 비밀번호를 비교한다.
3. 검증에 성공하면 Access Token과 Refresh Token을 발급한다.
4. 기존 Refresh Token을 삭제하고 새 Refresh Token을 저장한다.

## JWT 발급 흐름

`JwtService`는 노션 실습처럼 `Algorithm.HMAC256(secret)` 기반으로 토큰을 만든다.

- Access Token
  - subject: `memberId`
  - claim: `email`
  - 기본 만료 시간: 30분
- Refresh Token
  - subject: `memberId`
  - 기본 만료 시간: 2주

## Refresh Token 저장

`RefreshToken` 엔티티와 `RefreshTokenRepository`를 추가했다.

로그인 성공 시 기존 Refresh Token을 삭제하고 새 Refresh Token을 저장한다.
재발급 요청이 오면 DB에서 Refresh Token을 조회한 뒤 새 Access Token과 Refresh Token을 발급하고, 저장된 Refresh Token 값을 rotate한다.

## Spring Security + JWT Filter

`JwtAuthenticationFilter`는 모든 요청에서 `Authorization` 헤더를 확인한다.

```text
Authorization: Bearer <token>
```

토큰이 있으면 `JwtService.verifyAndGetMemberId(token)`으로 검증하고, 성공 시 `SecurityContextHolder`에 인증 정보를 저장한다.

```text
HTTP 요청
    ↓
JwtAuthenticationFilter
    ↓
SecurityContextHolder에 Authentication 저장
    ↓
SecurityConfig에서 인증 필요 여부 판단
    ↓
Controller
```

토큰이 없거나 유효하지 않으면 필터에서는 예외를 던지지 않고 다음 필터로 넘긴다.
최종적으로 인증이 필요한 API라면 Spring Security가 `401 Unauthorized`를 반환한다.

## 접근 정책

| 요청 | 인증 필요 여부 |
| --- | --- |
| `POST /api/v1/auth/login` | 필요 없음 |
| `POST /api/v1/auth/reissue` | 필요 없음 |
| `GET /posts` | 필요 없음 |
| `GET /posts/{id}` | 필요 없음 |
| `POST /posts` | 필요 |
| `PUT /posts/{id}` | 필요 |
| `DELETE /posts/{id}` | 필요 |
| `GET /api/v1/me` | 필요 |
| Swagger UI | 필요 없음 |

## 테스트 방법

### 1. 로그인

```bash
curl -i -X POST 'http://localhost:8080/api/v1/auth/login?email={DB에_있는_사용자_이메일}&password={DB에_있는_사용자_비밀번호}'
```

예상 결과:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "로그인 성공",
  "data": {
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

### 2. Access Token으로 내 정보 조회

```bash
curl -i http://localhost:8080/api/v1/me \
  -H 'Authorization: Bearer {accessToken}'
```

예상 결과:

- `200 OK`
- 인증된 사용자 id와 email 반환

### 3. 인증 없이 게시글 생성

```bash
curl -i -X POST http://localhost:8080/posts \
  -H 'Content-Type: application/json' \
  -d '{"title":"보안 실습","content":"인증 실패 확인","userId":1}'
```

예상 결과:

- `401 Unauthorized`
- `AUTH_001` 응답 반환

### 4. Access Token으로 게시글 생성

```bash
curl -i -X POST http://localhost:8080/posts \
  -H 'Authorization: Bearer {accessToken}' \
  -H 'Content-Type: application/json' \
  -d '{"title":"보안 실습","content":"인증 성공 확인","userId":1}'
```

예상 결과:

- JWT 인증은 통과한다.
- 요청 body의 `userId`에 해당하는 사용자가 DB에 있으면 `201 Created`가 반환된다.
- 해당 사용자가 없으면 기존 로직에 따라 `USER_001`이 반환된다.

### 5. Refresh Token으로 토큰 재발급

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/reissue \
  -H 'Authorization: Bearer {refreshToken}'
```

예상 결과:

- 새 Access Token과 새 Refresh Token 반환
- DB에 저장된 Refresh Token도 새 값으로 변경

## Swagger UI 테스트

Swagger UI는 인증 없이 접근할 수 있다.

```text
http://localhost:8080/swagger-ui/index.html
```

게시글 생성, 수정, 삭제 API에는 Bearer JWT 인증이 필요하도록 표시했다.
Swagger 상단의 `Authorize` 버튼에서는 `accessToken` 값만 넣으면 된다. HTTP Bearer 스키마이므로 Swagger가 `Bearer` prefix를 자동으로 붙인다.

```text
{accessToken}
```

## 이번 실습에서 확인한 점

Spring Security는 인증을 컨트롤러가 아니라 Filter Chain에서 먼저 처리한다.
따라서 인증 로직이 여러 컨트롤러에 흩어지지 않고, `JwtAuthenticationFilter` 한 곳에서 일관되게 처리된다.

이는 노션 실습에서 말한 것처럼 보안이 로깅, 트랜잭션처럼 여러 API에 걸쳐 적용되는 횡단 관심사이기 때문이다.
