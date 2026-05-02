# MySQL 및 DataGrip 설정 정리

## 개요

3주차 과제에서는 Spring Boot 애플리케이션을 MySQL 데이터베이스와 연결한다.

현재 기준으로는 MySQL 설치까지 완료했고, 애플리케이션에서 `root` 계정을 직접 사용하지 않기 위해 과제 전용 계정과 데이터베이스를 따로 만든다.

권장 구성은 다음과 같다.

- 코드 프로젝트 위치: `/Users/jhshin/IdeaProjects/assignment`
- DataGrip 프로젝트 위치: `~/DataGripProjects/assignment`
- 데이터베이스 이름: `assignment`
- MySQL 애플리케이션 계정: `assignment_jaehun`
- MySQL 서버 실행 방식: 필요할 때만 수동 실행

## MySQL 서버 수동 실행

MySQL을 항상 백그라운드에서 실행하지 않고, 실습할 때만 직접 켜고 끈다.

실습 시작 전에는 아래 명령어로 MySQL 서버를 실행한다.

```bash
mysql.server start
```

상태 확인은 아래 명령어로 한다.

```bash
mysql.server status
```

실습이 끝나면 아래 명령어로 MySQL 서버를 종료한다.

```bash
mysql.server stop
```

DataGrip에서 `Connection refused`가 발생하면 보통 MySQL 서버가 꺼져 있는 상태다. 이 경우 `mysql.server start`로 서버를 먼저 실행한 뒤 다시 연결을 테스트한다.

## 데이터베이스 및 계정 생성

먼저 `root` 계정으로 MySQL에 접속한다.

```bash
mysql -u root -p
```

비밀번호를 입력하고 `mysql>` 프롬프트가 뜨면 아래 SQL을 실행한다.

```sql
CREATE DATABASE IF NOT EXISTS assignment;

CREATE USER 'assignment_jaehun'@'localhost' IDENTIFIED BY '원하는비밀번호';

GRANT ALL PRIVILEGES ON assignment.* TO 'assignment_jaehun'@'localhost';

FLUSH PRIVILEGES;
```

위 설정은 `assignment_jaehun`이 `assignment` 데이터베이스에 대해서만 접근 권한을 갖도록 만든다.

작업이 끝나면 MySQL 콘솔을 종료한다.

```sql
exit;
```

## 새 계정 접속 확인

아래 명령어로 `assignment_jaehun` 계정 접속을 확인한다.

```bash
mysql -u assignment_jaehun -p assignment
```

비밀번호 입력 후 `mysql>` 프롬프트가 뜨면 정상적으로 설정된 것이다.

데이터베이스 목록을 확인하려면 아래 SQL을 실행한다.

```sql
SHOW DATABASES;
```

현재 사용할 데이터베이스를 확인하려면 아래 SQL을 실행한다.

```sql
SELECT DATABASE();
```

결과가 `assignment`이면 정상이다.

## DataGrip 프로젝트 설정

DataGrip 프로젝트는 코드 저장소와 분리해서 관리한다.

```text
Project Name: assignment
Location: ~/DataGripProjects/assignment
```

코드 프로젝트인 `/Users/jhshin/IdeaProjects/assignment` 안에 DataGrip 프로젝트 파일을 섞지 않는다.

## DataGrip Database Explorer 설정

DataGrip 왼쪽의 Database Explorer에서 아래 순서로 설정한다.

1. `+` 버튼 클릭
2. `Data Source` 선택
3. `MySQL` 선택
4. 연결 정보 입력
5. 필요한 경우 MySQL 드라이버 다운로드
6. `Test Connection` 실행
7. 성공하면 `Apply` 또는 `OK` 클릭

연결 정보는 아래와 같이 입력한다.

```text
Name: assignment
Host: 127.0.0.1
Port: 3306
User: assignment_jaehun
Password: 설정한 비밀번호
Database: assignment
```

`Host`는 `localhost` 대신 `127.0.0.1`을 우선 사용한다. 로컬 MySQL 연결에서 `localhost`가 소켓 연결로 처리되어 헷갈리는 경우를 줄일 수 있다.

## Spring Boot application.yml 설정

Spring Boot 애플리케이션도 DataGrip과 같은 데이터베이스 계정을 사용한다.

`application.yml`은 Git에서 추적되는 파일이므로 실제 DB 비밀번호를 직접 적지 않는다. 비밀번호는 실행 환경변수로 주입한다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/assignment
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${MYSQL_USERNAME:}
    password: ${MYSQL_PASSWORD:}

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    show-sql: true
```

## IntelliJ 실행 환경변수 설정

IntelliJ에서 초록색 실행 버튼으로 애플리케이션을 실행할 때도 환경변수를 넣을 수 있다.

설정 경로는 다음과 같다.

```text
Run/Debug Configurations
-> Edit Configurations...
-> Modify options
-> Environment variables
```

`Environment variables`에 아래 값을 추가한다.

```text
MYSQL_USERNAME=assignment_jaehun;MYSQL_PASSWORD=설정한 비밀번호
```

입력창 오른쪽의 아이콘을 누르면 표 형태로도 추가할 수 있다.

```text
MYSQL_USERNAME    assignment_jaehun
MYSQL_PASSWORD    설정한 비밀번호
```

실행 설정의 JDK 또는 JRE도 Java 17로 맞춘다. 이 프로젝트의 Gradle 8.7은 Java 26으로 실행하면 `Unsupported class file major version 70` 오류가 발생할 수 있다.

## 터미널 실행 환경변수 설정

터미널에서 실행할 때는 아래처럼 환경변수를 먼저 지정한 뒤 실행한다.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export MYSQL_USERNAME=assignment_jaehun
export MYSQL_PASSWORD='설정한 비밀번호'
./gradlew bootRun
```

## 실습 시작 순서

실습을 시작할 때는 아래 순서로 진행한다.

1. 터미널에서 MySQL 서버 실행

```bash
mysql.server start
```

2. DataGrip에서 `Test Connection` 확인
3. Spring Boot 애플리케이션 실행
4. API 테스트 또는 DB 데이터 확인

실습이 끝나면 아래 명령어로 MySQL 서버를 종료한다.

```bash
mysql.server stop
```

## 자주 발생하는 문제

### Connection refused

DataGrip에서 `Connection refused`가 발생하면 MySQL 서버가 꺼져 있을 가능성이 높다.

```bash
mysql.server start
```

서버 실행 후 다시 `Test Connection`을 누른다.

### Access denied

`Access denied`는 서버는 켜져 있지만 계정명, 비밀번호, 권한 중 하나가 맞지 않는 상황이다.

확인할 값은 다음과 같다.

- User가 `assignment_jaehun`인지 확인
- Password가 계정 생성 시 입력한 값과 같은지 확인
- Database가 `assignment`인지 확인
- `GRANT ALL PRIVILEGES ON assignment.* TO 'assignment_jaehun'@'localhost';`를 실행했는지 확인

### Unknown database 'assignment'

`assignment` 데이터베이스가 아직 생성되지 않은 상태다.

`root` 계정으로 접속한 뒤 아래 SQL을 실행한다.

```sql
CREATE DATABASE IF NOT EXISTS assignment;
```

## 최종 정리

관리 작업은 `root` 계정으로 수행하고, Spring Boot와 DataGrip 연결에는 `assignment_jaehun`을 사용한다.

이렇게 분리하면 `root` 권한을 애플리케이션에 직접 노출하지 않으면서도 과제용 데이터베이스만 깔끔하게 관리할 수 있다.
