# Postman 실패 케이스 정리

현재 구현 기준으로 Postman에서 바로 확인할 수 있는 실패 케이스를 정리한다.

## 1. 공통 안내

- `POST`, `PUT` 요청은 `Body -> raw -> JSON`으로 보낸다.
- `GET`, `DELETE` 요청은 body 없이 보낸다.
- 검증 실패는 `400 Bad Request`로 응답한다.
- 존재하지 않는 게시글 조회/수정/삭제는 `404 Not Found`로 응답한다.

## 2. 실패 응답 형식

### 2.1 잘못된 요청

상태 코드:

```text
400 Bad Request
```

응답 예시:

```json
{
  "success": false,
  "code": "COMMON_001",
  "message": "제목은 필수입니다.",
  "data": null
}
```

### 2.2 게시글 없음

상태 코드:

```text
404 Not Found
```

응답 예시:

```json
{
  "success": false,
  "code": "POST_001",
  "message": "게시글을 찾을 수 없습니다.",
  "data": null
}
```

## 3. POST /posts 실패 케이스

### 3.1 제목 누락

요청 body:

```json
{
  "title": "",
  "content": "내용입니다.",
  "author": "jaehun"
}
```

예상 결과:

- 상태 코드: `400 Bad Request`
- 메시지: `제목은 필수입니다.`

### 3.2 내용 누락

요청 body:

```json
{
  "title": "제목입니다.",
  "content": "",
  "author": "jaehun"
}
```

예상 결과:

- 상태 코드: `400 Bad Request`
- 메시지: `내용은 필수입니다.`

### 3.3 제목 50자 초과

요청 body:

```json
{
  "title": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "content": "내용입니다.",
  "author": "jaehun"
}
```

예상 결과:

- 상태 코드: `400 Bad Request`
- 메시지: `제목은 50자 이내로 작성해주세요.`

## 4. GET /posts/{id} 실패 케이스

### 4.1 존재하지 않는 게시글 조회

요청 URL 예시:

```text
GET /posts/999
```

요청 body:

```text
없음
```

예상 결과:

- 상태 코드: `404 Not Found`
- 메시지: `게시글을 찾을 수 없습니다.`

## 5. PUT /posts/{id} 실패 케이스

### 5.1 제목 누락

요청 URL 예시:

```text
PUT /posts/1
```

요청 body:

```json
{
  "title": "",
  "content": "수정 내용입니다."
}
```

예상 결과:

- 상태 코드: `400 Bad Request`
- 메시지: `제목은 필수입니다.`

### 5.2 내용 누락

요청 URL 예시:

```text
PUT /posts/1
```

요청 body:

```json
{
  "title": "수정 제목입니다.",
  "content": ""
}
```

예상 결과:

- 상태 코드: `400 Bad Request`
- 메시지: `내용은 필수입니다.`

### 5.3 제목 50자 초과

요청 URL 예시:

```text
PUT /posts/1
```

요청 body:

```json
{
  "title": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
  "content": "수정 내용입니다."
}
```

예상 결과:

- 상태 코드: `400 Bad Request`
- 메시지: `제목은 50자 이내로 작성해주세요.`

### 5.4 존재하지 않는 게시글 수정

요청 URL 예시:

```text
PUT /posts/999
```

요청 body:

```json
{
  "title": "수정 제목입니다.",
  "content": "수정 내용입니다."
}
```

예상 결과:

- 상태 코드: `404 Not Found`
- 메시지: `게시글을 찾을 수 없습니다.`

## 6. DELETE /posts/{id} 실패 케이스

### 6.1 존재하지 않는 게시글 삭제

요청 URL 예시:

```text
DELETE /posts/999
```

요청 body:

```text
없음
```

예상 결과:

- 상태 코드: `404 Not Found`
- 메시지: `게시글을 찾을 수 없습니다.`
