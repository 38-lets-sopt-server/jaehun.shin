# 빌드 트러블슈팅 가이드

## 1. 이번에 발생한 빌드 실패

### 증상

`./gradlew build` 실행 시 아래와 비슷한 오류가 발생했다.

```text
Execution failed for task ':compileJava'.
Failed to notify dependency resolution listener.
'java.util.Set org.gradle.api.artifacts.LenientConfiguration.getArtifacts(org.gradle.api.specs.Spec)'
```

스택트레이스 기준 핵심 예외는 아래였다.

```text
java.lang.NoSuchMethodError:
org.gradle.api.artifacts.LenientConfiguration.getArtifacts(...)
```

### 원인

프로젝트의 Spring 플러그인 버전과 Gradle 래퍼 버전이 맞지 않았다.

- `build.gradle`
  - `org.springframework.boot` `3.2.4`
  - `io.spring.dependency-management` `1.1.4`
- `gradle/wrapper/gradle-wrapper.properties`
  - 기존 `distributionUrl`: `gradle-9.4.0-bin.zip`

즉, 프로젝트는 Spring Boot 3.2.4 조합인데 래퍼가 Gradle 9.4.0을 실행하면서, 플러그인이 기대하던 Gradle API와 실제 실행 중인 Gradle API가 달라져 빌드가 깨졌다.

## 2. 해결 방법

`gradle/wrapper/gradle-wrapper.properties`의 `distributionUrl`을 Gradle 8 계열로 변경했다.

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
```

변경 후 다시 실행:

```bash
./gradlew build
```

## 3. 왜 이 설정으로 해결됐는가

`./gradlew`는 시스템에 설치된 Gradle이 아니라, `gradle-wrapper.properties`에 적힌 버전의 Gradle을 내려받아 실행한다.  
따라서 `distributionUrl`을 바꾸면 같은 `./gradlew build` 명령이라도 실제로는 다른 Gradle 버전으로 빌드하게 된다.

이번 문제는 자바 소스 코드 오류가 아니라, 빌드 도구 버전 호환성 문제였다.

## 4. 다시 같은 문제가 생기면 확인할 것

### 1) 래퍼 버전 확인

```bash
sed -n '1,20p' gradle/wrapper/gradle-wrapper.properties
```

`distributionUrl`이 `gradle-9.x`로 올라가 있으면 먼저 의심한다.

### 2) 플러그인 버전 확인

```bash
sed -n '1,40p' build.gradle
```

다음 두 버전을 같이 본다.

- `org.springframework.boot`
- `io.spring.dependency-management`

Gradle을 올리거나 내릴 때는 이 플러그인 조합과 함께 봐야 한다.

### 3) 실제 오류 메시지 확인

```bash
./gradlew build --stacktrace
```

오류에 아래 문자열이 보이면 이번과 같은 유형일 가능성이 높다.

- `Failed to notify dependency resolution listener`
- `NoSuchMethodError`
- `LenientConfiguration.getArtifacts`

## 5. 현재 기준 권장 확인 순서

1. `gradle/wrapper/gradle-wrapper.properties`에서 Gradle 버전을 먼저 확인한다.
2. `build.gradle`의 Spring 플러그인 버전과 같이 본다.
3. `./gradlew build --stacktrace`로 실제 실패 지점을 확인한다.
4. 도구 버전 문제인지, 소스 코드 문제인지 분리해서 판단한다.

## 6. 이번에 함께 확인된 후속 이슈

Gradle 9 문제를 해결한 뒤에는 로컬 환경의 Java 버전 때문에 아래 오류가 이어서 발생할 수 있었다.

```text
Execution failed for task ':resolveMainClassName'.
Unsupported class file major version 70
```

이번 저장소에서 확인한 환경은 `OpenJDK 26`이었다.  
즉, `Gradle 9 -> 8.7` 변경은 첫 번째 문제를 해결했지만, Java가 너무 최신 버전이면 별도의 호환성 문제가 이어질 수 있다.

이 오류가 다시 나오면 아래도 함께 확인한다.

```bash
java -version
./gradlew --version
```

## 7. 빠른 결론

- 이번 빌드 실패의 1차 원인은 `Gradle 9.4.0`과 Spring 플러그인 조합의 호환성 문제였다.
- 해결은 `distributionUrl`을 `Gradle 8.7`로 내리는 것이었다.
- 이후에도 빌드가 안 되면, 다음으로는 로컬 Java 버전을 확인해야 한다.
