# 화면설계서 기능 정리
![](img/everytime-storyboard.png)

## 1. 게시글에 필요하다고 생각하는 기능

- 게시글 목록 조회
- 게시글 상세 조회
- 게시글 작성
- 제목, 본문 입력
- 익명 여부 선택
- 질문 태그 선택
- 이미지 또는 링크 첨부
- 공감
- 댓글
- 대댓글
- 스크랩
- 게시글 작성 시각 표시
- 댓글 작성 시각 표시
- 게시글별 공감 수, 댓글 수, 스크랩 수 표시
- 내가 공감했는지, 스크랩했는지 상태 표시
- 댓글 작성 시 익명 여부 선택
- 스크롤 기반 목록 조회 또는 페이지네이션

추가로 화면상 보이는 요소까지 포함하면 공지, 인기글, 광고 영역도 있지만, 핵심 과제 기준으로는 게시글/댓글 중심 기능이 우선입니다.

## 2. 어떤 데이터를 저장해야 하는가

- 사용자
    - `userId`
    - `학교/커뮤니티 소속 정보`
    - `상태값(정상, 정지 등)`

- 게시판
    - `boardId`
    - `boardName`
    - `schoolId`

- 게시글
    - `postId`
    - `boardId`
    - `authorUserId`
    - `title`
    - `content`
    - `isAnonymous`
    - `isQuestion`
    - `createdAt`
    - `updatedAt`
    - `deletedAt`
    - `likeCount`
    - `commentCount`
    - `scrapCount`

- 댓글
    - `commentId`
    - `postId`
    - `parentCommentId` (`null`이면 일반 댓글, 값이 있으면 대댓글)
    - `authorUserId`
    - `content`
    - `isAnonymous`
    - `createdAt`
    - `updatedAt`
    - `deletedAt`

- 공감
    - `postLikeId`
    - `postId`
    - `userId`
    - `createdAt`

- 스크랩
    - `scrapId`
    - `postId`
    - `userId`
    - `createdAt`

- 첨부파일
    - `attachmentId`
    - `postId`
    - `type` (`image`, `link`)
    - `url`
    - `createdAt`

- 익명 표시용 정보
    - 내부적으로는 `authorUserId`를 저장해야 함
    - 화면에는 `익명`, `익명1`, `익명2`처럼 표시할 규칙이 필요함

## 3. 어떤 요청/응답 데이터를 주고받아야 하는가

- 게시글 목록 조회
    - 요청: `boardId`, `page`, `size`
    - 응답: `postId`, `title`, `contentPreview`, `authorDisplayName`, `createdAt`, `likeCount`, `commentCount`, `isAnonymous`

- 게시글 상세 조회
    - 요청: `postId`
    - 응답: 게시글 본문 정보 + 댓글 목록
    - 댓글 응답에는 `commentId`, `parentCommentId`, `content`, `authorDisplayName`, `createdAt`가 필요함

- 게시글 작성
    - 요청: `title`, `content`, `isAnonymous`, `isQuestion`, `attachmentIds`
    - 응답: `postId`, `createdAt`

- 댓글 작성
    - 요청: `postId`, `content`, `isAnonymous`
    - 응답: `commentId`, `createdAt`, `authorDisplayName`

- 대댓글 작성
    - 요청: `postId`, `parentCommentId`, `content`, `isAnonymous`
    - 응답: `commentId`, `parentCommentId`, `createdAt`

- 공감 등록/취소
    - 요청: `postId`
    - 응답: `postId`, `likeCount`, `likedByMe`

- 스크랩 등록/취소
    - 요청: `postId`
    - 응답: `postId`, `scrappedByMe`

- 첨부 업로드
    - 요청: 파일 데이터 또는 링크 주소
    - 응답: `attachmentId`, `url`

실무적으로는 상세 조회 응답에 `likedByMe`, `scrappedByMe` 같은 “내 상태” 값도 같이 주는 것이 편합니다.

## 4. 어떤 조건을 검증해야 하는가

- 제목은 필수인지 검증해야 함
- 본문은 비어 있으면 안 됨
- 제목/본문 글자 수 제한이 필요함
- 공백만 입력한 경우 저장되면 안 됨
- 게시판이 실제 존재하는지 검증해야 함
- 로그인한 사용자만 글쓰기/댓글/공감/스크랩이 가능해야 함
- 같은 사용자가 같은 게시글에 공감을 중복으로 하면 안 됨
- 같은 사용자가 같은 게시글을 중복 스크랩하면 안 됨
- 대댓글의 `parentCommentId`가 해당 게시글의 댓글인지 검증해야 함
- 삭제된 게시글에는 댓글 작성이 막혀야 함
- 삭제된 댓글에는 대댓글 작성 정책을 정해야 함
- 익명 체크 여부와 관계없이 내부적으로 작성자는 추적 가능해야 함
- 화면에는 실명이 노출되지 않도록 검증해야 함
- 첨부파일은 파일 형식, 크기, 개수 제한이 필요함
- 비속어, 홍보성 글, 금지어 등 운영 정책 검증이 필요할 수 있음
- 시간 정보는 클라이언트가 아니라 서버 기준으로 저장하는 것이 안전함

