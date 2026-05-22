# 6차 과제 인증/인가 구현 문서

## 1. 구현 목표

19번 이슈는 6주차 과제 노션 페이지의 필수 과제 2개와 심화 과제 2개를 에브리타임 클론 프로젝트에 적용하는 작업이다.

이번 구현의 핵심 목표는 다음과 같다.

- JWT와 Spring Security를 프로젝트 전체 인증 흐름에 적용한다.
- 로그인, 토큰 재발급, 회원가입, 소셜 로그인처럼 인증 없이 접근해야 하는 API와 게시글 작성/수정/삭제, 좋아요, 로그아웃처럼 인증이 필요한 API를 분리한다.
- 비밀번호는 평문으로 저장하지 않고 `BCryptPasswordEncoder`로 암호화한다.
- Refresh Token은 DB에 저장하고 재발급 때마다 새 토큰으로 교체해서 재사용을 막는다.
- 로그아웃 시 Refresh Token을 삭제하고 현재 Access Token을 블랙리스트에 추가해서 만료 전에도 재사용할 수 없게 한다.
- Kakao와 Google 소셜 로그인 모두를 지원하고, 외부 인증 서버에서 받은 사용자 정보를 기준으로 신규 회원가입 또는 기존 회원 로그인을 처리한다.

## 2. 요구사항 대응표

| 구분 | 요구사항 | 구현 내용 |
| --- | --- | --- |
| 필수 1 | JWT + Spring Security 인증을 프로젝트 전체에 적용 | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`를 통해 JWT 기반 stateless 인증을 적용했다. |
| 필수 1 | 로그인/토큰 재발급 API는 인증 없이 접근 | `/api/v1/auth/login`, `/api/v1/auth/reissue`를 `permitAll`로 설정했다. |
| 필수 1 | 게시글 작성/수정/삭제는 인증 필요 | `POST /posts`, `PUT /posts/{id}`, `DELETE /posts/{id}`를 인증 필요 API로 설정했다. |
| 필수 1 | 좋아요 추가/취소는 인증 필요 | `POST /posts/{id}/likes`, `DELETE /posts/{id}/likes`를 인증 필요 API로 추가했다. |
| 필수 2 | 비밀번호 암호화 저장 | `POST /api/v1/auth/signup`에서 BCrypt로 암호화한 비밀번호만 저장한다. |
| 필수 2 | 로그인 비밀번호 검증 | 로그인 시 `PasswordEncoder.matches()`로 입력 비밀번호와 저장된 BCrypt 해시를 비교한다. |
| 심화 1 | 로그아웃 API 구현 | `POST /api/v1/auth/logout`을 추가했다. |
| 심화 1 | 로그아웃 시 Refresh Token 삭제 | 로그아웃하면 `refresh_token` 테이블에서 해당 유저의 Refresh Token을 삭제한다. |
| 심화 1 | 현재 Access Token 블랙리스트 처리 | 로그아웃 요청에 사용된 Access Token을 `access_token_blacklist` 테이블에 저장하고 이후 인증 필터에서 차단한다. |
| 심화 1 | Access Token 만료 시 클라이언트 401 처리 흐름 작성 | 이 문서의 "9. 클라이언트 401 처리 흐름"에 정리했다. |
| 심화 2 | Kakao 또는 Google OAuth 2.0 소셜 로그인 | Kakao와 Google을 모두 구현했다. |
| 심화 2 | 외부 인증 서버 유저 정보 조회 | Kakao user info API, Google userinfo API를 호출하는 `OAuthUserInfoClient`를 추가했다. |
| 심화 2 | 소셜 로그인 신규 유저 자동 회원가입 | SocialAccount와 email 기존 회원이 없으면 `User`를 자동 생성한다. |
| 심화 2 | 소셜 로그인 기존 유저 로그인 | 기존 SocialAccount가 있으면 해당 User로 로그인하고, 없더라도 같은 email의 User가 있으면 SocialAccount를 연결한다. |
| 심화 2 | 소셜 로그인 성공 시 Access/Refresh Token 발급 | 일반 로그인과 동일하게 Access Token과 Refresh Token을 발급하고 Refresh Token을 DB에 저장한다. |

## 3. 주요 구조

### 인증 구성

- `SecurityConfig`
  - Spring Security filter chain을 구성한다.
  - 세션, form login, http basic, csrf를 비활성화하고 JWT 기반 stateless 인증을 사용한다.
  - `PasswordEncoder` bean으로 `BCryptPasswordEncoder`를 등록한다.
- `JwtAuthenticationFilter`
  - 모든 요청에서 `Authorization: Bearer {token}` 헤더를 확인한다.
  - JWT 서명과 만료 시간을 검증한다.
  - Access Token 블랙리스트에 포함된 토큰이면 인증을 만들지 않는다.
  - 정상 토큰이면 `SecurityContextHolder`에 인증 사용자 id를 저장한다.
- `JwtService`
  - Access Token과 Refresh Token을 발급한다.
  - 토큰 검증 후 subject에 들어 있는 사용자 id를 반환한다.
  - 로그아웃 블랙리스트 저장을 위해 Access Token 만료 시각도 읽는다.

### 인증 도메인

- `User`
  - 일반 회원과 소셜 로그인 회원 모두의 기준 사용자 테이블이다.
  - 일반 회원은 `password`에 BCrypt 해시가 저장된다.
  - 소셜 로그인으로 자동 생성된 회원은 password가 `null`일 수 있다.
- `RefreshToken`
  - Refresh Token을 DB에 저장한다.
  - 로그인과 재발급 시 기존 토큰을 삭제하거나 새 토큰으로 교체한다.
- `AccessTokenBlacklist`
  - 로그아웃된 Access Token을 저장한다.
  - 필터는 여기에 저장된 Access Token을 인증에 사용하지 않는다.
- `SocialAccount`
  - Kakao/Google provider와 외부 사용자 id를 내부 `User`와 연결한다.
  - `(provider, providerUserId)` 조합을 유니크하게 유지한다.
- `PostLike`
  - 게시글과 사용자 사이의 좋아요 관계를 저장한다.
  - `(post_id, user_id)` 조합을 유니크하게 유지해 중복 좋아요를 막는다.

## 4. API 명세

공통 응답 형식은 기존 `BaseResponse`를 그대로 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "처리 메시지",
  "data": {}
}
```

실패 응답도 같은 형식을 사용한다.

```json
{
  "success": false,
  "code": "AUTH_001",
  "message": "인증이 필요합니다.",
  "data": null
}
```

### 회원가입

```http
POST /api/v1/auth/signup
Content-Type: application/json
```

```json
{
  "nickname": "홍길동",
  "email": "hong@example.com",
  "password": "password123"
}
```

처리 흐름:

1. nickname, email, password가 비어 있는지 검증한다.
2. 이미 가입된 email인지 확인한다.
3. password를 BCrypt로 암호화한다.
4. 암호화된 password를 `users.password`에 저장한다.

### 로그인

```http
POST /api/v1/auth/login?email=hong@example.com&password=password123
```

처리 흐름:

1. email로 사용자를 조회한다.
2. 저장된 BCrypt 해시와 요청 password를 `matches()`로 비교한다.
3. 검증에 성공하면 Access Token과 Refresh Token을 발급한다.
4. 기존 Refresh Token을 삭제하고 새 Refresh Token을 저장한다.

로그인 실패는 `401 Unauthorized`와 `AUTH_002`로 응답한다.

### 토큰 재발급

```http
POST /api/v1/auth/reissue
Authorization: Bearer {refreshToken}
```

처리 흐름:

1. Refresh Token의 JWT 서명과 만료 시간을 검증한다.
2. DB에 같은 Refresh Token이 저장되어 있는지 확인한다.
3. DB에 저장된 Refresh Token이 만료되지 않았는지 확인한다.
4. 새 Access Token과 새 Refresh Token을 발급한다.
5. DB의 Refresh Token 값을 새 Refresh Token으로 교체한다.

이 방식은 이미 사용된 Refresh Token을 다시 사용할 수 없게 만드는 rotate 전략이다.

### 로그아웃

```http
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}
```

처리 흐름:

1. 인증 필터가 Access Token을 검증하고 현재 사용자 id를 `SecurityContext`에 저장한다.
2. 로그아웃 서비스에서 요청 토큰의 subject와 인증 사용자 id가 같은지 다시 확인한다.
3. `refresh_token` 테이블에서 해당 사용자의 Refresh Token을 삭제한다.
4. 현재 Access Token과 만료 시각을 `access_token_blacklist` 테이블에 저장한다.
5. 이후 같은 Access Token으로 보호 API를 호출하면 인증이 생성되지 않아 401이 반환된다.

### 내 정보 조회

```http
GET /api/v1/me
Authorization: Bearer {accessToken}
```

인증된 사용자 id로 `User`를 조회해 사용자 정보를 반환한다.

### Kakao 소셜 로그인

```http
POST /api/v1/auth/oauth/kakao
Content-Type: application/json
```

```json
{
  "accessToken": "{kakaoAccessToken}"
}
```

처리 흐름:

1. 클라이언트가 Kakao에서 발급받은 access token을 서버로 전달한다.
2. 서버는 Kakao user info API를 호출한다.
3. 응답에서 `id`, `kakao_account.email`, `kakao_account.profile.nickname`을 읽는다.
4. 기존 `SocialAccount(KAKAO, id)`가 있으면 해당 User로 로그인한다.
5. 없으면 같은 email의 기존 User를 찾아 SocialAccount를 연결한다.
6. 같은 email의 User도 없으면 신규 User를 자동 생성하고 SocialAccount를 연결한다.
7. 우리 서버의 Access Token과 Refresh Token을 발급한다.

### Google 소셜 로그인

```http
POST /api/v1/auth/oauth/google
Content-Type: application/json
```

```json
{
  "accessToken": "{googleAccessToken}"
}
```

처리 흐름:

1. 클라이언트가 Google에서 발급받은 access token을 서버로 전달한다.
2. 서버는 Google userinfo API를 호출한다.
3. 응답에서 `sub`, `email`, `name`을 읽는다.
4. 기존 `SocialAccount(GOOGLE, sub)`가 있으면 해당 User로 로그인한다.
5. 없으면 같은 email의 기존 User를 찾아 SocialAccount를 연결한다.
6. 같은 email의 User도 없으면 신규 User를 자동 생성하고 SocialAccount를 연결한다.
7. 우리 서버의 Access Token과 Refresh Token을 발급한다.

### 게시글 작성

```http
POST /posts
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "title": "제목",
  "content": "내용"
}
```

기존 `CreatePostRequest`에는 `userId` 필드가 남아 있지만, 이제 게시글 작성자는 요청 body의 `userId`가 아니라 인증된 사용자 id로 결정된다.

### 게시글 수정/삭제

```http
PUT /posts/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "title": "수정 제목",
  "content": "수정 내용"
}
```

```http
DELETE /posts/{id}
Authorization: Bearer {accessToken}
```

수정/삭제는 인증된 사용자와 게시글 작성자가 같을 때만 허용된다. 작성자가 아니면 `403 Forbidden`과 `AUTH_003`을 반환한다.

### 게시글 좋아요 추가/취소

```http
POST /posts/{id}/likes
Authorization: Bearer {accessToken}
```

```http
DELETE /posts/{id}/likes
Authorization: Bearer {accessToken}
```

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "게시글 좋아요 성공",
  "data": {
    "postId": 1,
    "likeCount": 3,
    "likedByMe": true
  }
}
```

좋아요 추가는 이미 좋아요한 상태에서도 중복 row를 만들지 않는다. 좋아요 취소는 좋아요하지 않은 상태에서도 실패하지 않고 `likedByMe: false`로 응답한다.

## 5. 접근 권한 정책

| 요청 | 인증 필요 여부 |
| --- | --- |
| `POST /api/v1/auth/signup` | 필요 없음 |
| `POST /api/v1/auth/login` | 필요 없음 |
| `POST /api/v1/auth/reissue` | 필요 없음 |
| `POST /api/v1/auth/oauth/kakao` | 필요 없음 |
| `POST /api/v1/auth/oauth/google` | 필요 없음 |
| `GET /posts` | 필요 없음 |
| `GET /posts/{id}` | 필요 없음 |
| `POST /posts` | 필요 |
| `PUT /posts/{id}` | 필요 |
| `DELETE /posts/{id}` | 필요 |
| `POST /posts/{id}/likes` | 필요 |
| `DELETE /posts/{id}/likes` | 필요 |
| `POST /api/v1/auth/logout` | 필요 |
| `GET /api/v1/me` | 필요 |
| Swagger UI, OpenAPI docs | 필요 없음 |

## 6. DB 구조

이번 과제에서 사용하거나 추가한 주요 테이블은 다음과 같다. `ddl-auto:update`를 사용하므로 별도 migration 파일은 만들지 않았다.

### users

| 컬럼 | 설명 |
| --- | --- |
| `id` | 내부 사용자 id |
| `nickname` | 사용자 닉네임 |
| `email` | 사용자 이메일, 일반 회원과 소셜 회원 연결 기준 |
| `password` | BCrypt 해시, 소셜 전용 회원은 null 가능 |
| `created_at`, `updated_at` | 생성/수정 시각 |

기존에 평문 password로 저장된 사용자는 BCrypt `matches()` 검증에 실패한다. 회원가입 API로 새 사용자를 만들거나 DB의 password 값을 BCrypt 해시로 교체해야 한다.

### refresh_token

| 컬럼 | 설명 |
| --- | --- |
| `id` | Refresh Token row id |
| `member_id` | 사용자 id |
| `token` | Refresh Token 문자열 |
| `expires_at` | 서버 기준 만료 시각 |

로그인 시 기존 Refresh Token을 삭제하고 새 토큰을 저장한다. 재발급 시에는 저장된 토큰 값을 새 Refresh Token으로 교체한다.

### access_token_blacklist

| 컬럼 | 설명 |
| --- | --- |
| `id` | 블랙리스트 row id |
| `member_id` | 로그아웃한 사용자 id |
| `token` | 더 이상 사용할 수 없는 Access Token |
| `expires_at` | 해당 Access Token의 원래 만료 시각 |

현재 구현은 블랙리스트 조회 차단에 집중한다. `expires_at`을 저장해두었으므로 이후 스케줄러로 만료된 블랙리스트 row를 정리하는 확장이 가능하다.

### social_accounts

| 컬럼 | 설명 |
| --- | --- |
| `id` | SocialAccount row id |
| `provider` | `KAKAO` 또는 `GOOGLE` |
| `provider_user_id` | Kakao `id`, Google `sub` |
| `user_id` | 내부 User id |

`provider`와 `provider_user_id` 조합은 유니크하다.

### post_likes

| 컬럼 | 설명 |
| --- | --- |
| `id` | 좋아요 row id |
| `post_id` | 게시글 id |
| `user_id` | 좋아요한 사용자 id |
| `created_at`, `updated_at` | 생성/수정 시각 |

`post_id`와 `user_id` 조합은 유니크하다.

## 7. JWT 인증 흐름

보호 API 호출 흐름은 다음과 같다.

```text
클라이언트 요청
  -> Authorization 헤더에서 Bearer Token 추출
  -> Access Token 블랙리스트 조회
  -> JWT 서명과 만료 시간 검증
  -> subject의 사용자 id 추출
  -> SecurityContext에 Authentication 저장
  -> Controller에서 Authentication.getName()으로 사용자 id 사용
```

토큰이 없거나, 만료되었거나, 블랙리스트에 있거나, 서명이 올바르지 않으면 `SecurityContext`에 인증 정보가 저장되지 않는다. 인증이 필요한 API라면 Spring Security의 `AuthenticationEntryPoint`가 `401 Unauthorized`를 반환한다.

## 8. Refresh Token 재사용 방지

Refresh Token 재사용 방지는 DB 저장값과 rotate 전략으로 처리한다.

```text
로그인 성공
  -> 기존 Refresh Token 삭제
  -> 새 Refresh Token 저장

토큰 재발급 성공
  -> 요청 Refresh Token이 DB에 있는지 확인
  -> 새 Access Token 발급
  -> 새 Refresh Token 발급
  -> DB의 Refresh Token 값을 새 토큰으로 교체
```

재발급이 한 번 성공하면 이전 Refresh Token은 DB에 더 이상 존재하지 않는다. 따라서 이전 Refresh Token을 다시 보내면 `AUTH_002` 응답을 받는다.

## 9. 클라이언트 401 처리 흐름

Access Token이 만료되었거나 로그아웃으로 블랙리스트 처리된 경우 서버는 보호 API에서 401을 반환한다. 클라이언트는 다음 흐름으로 처리할 수 있다.

```text
보호 API 요청
  -> 200이면 정상 처리
  -> 401이면 Access Token 사용 불가로 판단
      -> Refresh Token이 있으면 /api/v1/auth/reissue 호출
          -> 재발급 성공: 새 Access/Refresh Token 저장 후 원래 요청 재시도
          -> 재발급 실패: 저장된 토큰 삭제 후 로그인 페이지로 이동
      -> Refresh Token이 없으면 저장된 토큰 삭제 후 로그인 페이지로 이동
```

주의할 점:

- 로그아웃 후에는 Refresh Token이 삭제되므로 재발급도 실패한다.
- Access Token이 블랙리스트에 있으면 만료 전이라도 보호 API에서 401이 반환된다.
- 재발급 성공 후에는 반드시 기존 Refresh Token을 새 Refresh Token으로 교체해야 한다.

## 10. 환경변수

기본값은 `application.yml`에 있고, 운영 환경에서는 환경변수로 덮어쓴다.

```bash
MYSQL_URL=jdbc:mysql://localhost:3306/assignment
MYSQL_USERNAME=
MYSQL_PASSWORD=
JWT_SECRET=assignment-security-practice-jwt-secret-key-2026
JWT_ACCESS_TOKEN_EXPIRES_IN_SECONDS=1800
JWT_REFRESH_TOKEN_EXPIRES_IN_SECONDS=1209600
KAKAO_USER_INFO_URI=https://kapi.kakao.com/v2/user/me
GOOGLE_USER_INFO_URI=https://www.googleapis.com/oauth2/v3/userinfo
```

소셜 로그인에서 서버는 클라이언트가 전달한 Kakao 또는 Google access token으로 외부 사용자 정보 API만 호출한다. 인가 코드 교환은 이번 서버 구현 범위에 포함하지 않았다.

## 11. curl 검증 시나리오

아래 예시는 로컬 서버가 `http://localhost:8080`에서 실행 중이라고 가정한다.

### 1. 회원가입

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"nickname":"홍길동","email":"hong@example.com","password":"password123"}'
```

확인할 점:

- `200 OK`
- DB의 `users.password`가 평문이 아니라 `$2a$`, `$2b$`, `$2y$` 등 BCrypt 해시 형태인지 확인

### 2. 로그인

```bash
curl -i -X POST 'http://localhost:8080/api/v1/auth/login?email=hong@example.com&password=password123'
```

응답의 `data.accessToken`, `data.refreshToken`을 각각 저장한다.

### 3. 인증 없이 게시글 작성 실패

```bash
curl -i -X POST http://localhost:8080/posts \
  -H 'Content-Type: application/json' \
  -d '{"title":"인증 실패 테스트","content":"토큰 없이 작성"}'
```

예상 결과:

- `401 Unauthorized`
- `AUTH_001`

### 4. 인증 후 게시글 작성 성공

```bash
curl -i -X POST http://localhost:8080/posts \
  -H 'Authorization: Bearer {accessToken}' \
  -H 'Content-Type: application/json' \
  -d '{"title":"인증 성공 테스트","content":"토큰으로 작성"}'
```

확인할 점:

- `201 Created`
- 요청 body에 `userId`를 넣지 않아도 인증 사용자로 게시글이 생성된다.

### 5. 좋아요 추가

```bash
curl -i -X POST http://localhost:8080/posts/{postId}/likes \
  -H 'Authorization: Bearer {accessToken}'
```

예상 결과:

- `likedByMe: true`
- `likeCount` 증가

같은 요청을 한 번 더 보내도 `post_likes` row가 중복 생성되지 않는다.

### 6. 좋아요 취소

```bash
curl -i -X DELETE http://localhost:8080/posts/{postId}/likes \
  -H 'Authorization: Bearer {accessToken}'
```

예상 결과:

- `likedByMe: false`
- 좋아요가 이미 없는 상태에서 다시 호출해도 실패하지 않는다.

### 7. Refresh Token 재발급

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/reissue \
  -H 'Authorization: Bearer {refreshToken}'
```

확인할 점:

- 새 Access Token과 새 Refresh Token이 발급된다.
- 같은 기존 Refresh Token으로 다시 재발급을 시도하면 실패한다.

### 8. 로그아웃

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/logout \
  -H 'Authorization: Bearer {accessToken}'
```

확인할 점:

- 해당 사용자의 Refresh Token이 DB에서 삭제된다.
- 사용한 Access Token이 `access_token_blacklist`에 저장된다.

### 9. 로그아웃한 Access Token 재사용 실패

```bash
curl -i http://localhost:8080/api/v1/me \
  -H 'Authorization: Bearer {accessToken}'
```

예상 결과:

- `401 Unauthorized`
- `AUTH_001`

### 10. Kakao 소셜 로그인

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/oauth/kakao \
  -H 'Content-Type: application/json' \
  -d '{"accessToken":"{kakaoAccessToken}"}'
```

확인할 점:

- 신규 사용자라면 `users`, `social_accounts`, `refresh_token`에 row가 생성된다.
- 기존 사용자라면 기존 `SocialAccount` 또는 같은 email의 `User`를 기준으로 로그인된다.
- 응답으로 우리 서버의 Access Token과 Refresh Token이 반환된다.

### 11. Google 소셜 로그인

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/oauth/google \
  -H 'Content-Type: application/json' \
  -d '{"accessToken":"{googleAccessToken}"}'
```

확인할 점은 Kakao 소셜 로그인과 동일하다.

## 12. PR 작성 시 참고할 내용

키워드 과제는 PR 본문에 별도 정리하면 된다.

- OAuth 2.0은 사용자가 비밀번호를 직접 서비스에 넘기지 않고, 인가 서버가 발급한 토큰으로 보호 자원에 접근하게 해주는 권한 위임 표준이다.
- 이번 구현에서는 클라이언트가 Kakao/Google에서 access token을 받은 뒤 서버에 전달하고, 서버가 그 access token으로 사용자 정보 API를 호출하는 흐름을 사용했다.
- 서버는 외부 provider의 토큰을 그대로 서비스 인증 토큰으로 쓰지 않고, 내부 사용자 식별과 연결을 마친 뒤 자체 JWT를 새로 발급한다.
