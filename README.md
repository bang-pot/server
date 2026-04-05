# BangPot Backend

BangPot의 `auth` 기능과 공통 운영 기반을 관리하는 backend 저장소입니다.

## Stack

- Java 21
- Spring Boot
- Spring Security OAuth2 Client
- Spring Boot Actuator
- Gradle
- PostgreSQL

## Local setup

1. `.env.example`을 `.env`로 복사합니다.
2. 로컬 PostgreSQL 접속 정보, JWT, Kakao OAuth, frontend base URL, required terms version 값을 채웁니다.
3. 로컬 secret 값은 `src/main/resources/application-local-secret.yml`에 작성합니다.
4. Kakao developer console에 `http://localhost:8080/login/oauth2/code/kakao`를 등록합니다.
5. 기본 `local` profile로 애플리케이션을 실행합니다.

`local` profile은 `src/main/resources/application-local-secret.yml`을 직접 읽습니다.

### PowerShell example

```powershell
Copy-Item .env.example .env
./gradlew.bat bootRun
```

## Profiles

- `local`: 로컬 `.env`와 `src/main/resources/application-local-secret.yml`을 함께 읽고, SQL logging을 켜고, `ddl-auto=update`, JWT secure cookie 기본값은 `false`를 사용합니다.
- `prod`: environment variable 기반 datasource / auth 설정을 사용하고, SQL logging을 끄고, `ddl-auto=validate`, JWT secure cookie는 운영 기준값을 따릅니다.

## Operational logging

- 모든 요청은 `X-Request-Id`를 재사용하거나 새로 발급받고, 응답 헤더로도 같은 값을 돌려줍니다.
- 공통 요청 로그에는 `method`, `path`, `status`, `durationMs`가 포함됩니다.
- `local`은 개발 편의를 위해 `com.bangpot=DEBUG`와 SQL logging을 사용합니다.
- `prod`는 운영 로그를 짧고 읽기 쉽게 유지하기 위해 `INFO` 기본값과 SQL logging off 기준을 사용합니다.
- console log는 `timestamp`, `level`, `service`, `env`, `requestId`, `logger`, `message` 공통 형식을 사용합니다.
- request log와 auth audit log에는 raw token, cookie, secret, request body를 남기지 않습니다.

## Slack 운영 알림

Round 02부터 Slack 운영 알림은 appender 기반으로 동작합니다.

- `all-log` 채널
  - startup 완료 로그
  - 요청 처리 중 `5xx` 서버 오류 로그
  - 인증/권한 치명 이벤트 로그
- `error-log` 채널
  - 모든 `ERROR` 로그
  - 운영 서버 startup 실패
  - startup 직후 데이터베이스 확인 실패

현재 Slack 대상 이벤트는 아래 범위로 제한합니다.

- backend `5xx`
- 운영 서버 startup 실패
- startup 직후 데이터베이스 확인 실패
- `auth.login.failure`
- `auth.access_denied`

Slack에서 제외하는 항목은 아래와 같습니다.

- 모든 일반 `4xx`
- `auth.protected_resource_access_failed`
  - AWS 공개 환경의 스캔성 `401` 요청이 많아서 Slack에는 보내지 않고 로그와 운영 문서 기준으로만 확인합니다.
- `auth.login.success`, `auth.logout`, `auth.state.changed`
- threshold, dedupe, 채널 분리 자동화

Slack payload에는 아래 최소 필드만 포함합니다.

- `timestamp`
- `service`
- `environment`
- `requestId`
- `path` 또는 `context`
- `summary`

현재 구현은 운영 가독성을 위해 `reason` 필드를 추가로 포함합니다.

Slack payload에는 raw token, cookie, secret, query string, request body, 개인정보 raw 값을 넣지 않습니다.

### Slack 설정

`prod`에서는 아래 environment variable을 사용합니다.

- `BANGPOT_OPS_SLACK_ALL_LOG_WEBHOOK_URL`
- `BANGPOT_OPS_SLACK_ERROR_LOG_WEBHOOK_URL`

`local`에서 직접 확인하려면 `src/main/resources/application-local-secret.yml`에 아래 값을 추가합니다.

```yml
bangpot:
  ops:
    slack:
      all-log-webhook-url: https://hooks.slack.com/services/...
      error-log-webhook-url: https://hooks.slack.com/services/...
```

## Auth and health endpoints

- `GET /api/auth/me`
- `GET /api/auth/nickname-availability`
- `POST /api/auth/complete`
- `POST /api/auth/logout`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /oauth2/authorization/kakao`
- `GET /login/oauth2/code/kakao`

## Verification commands

```powershell
./gradlew.bat lint
./gradlew.bat test
./gradlew.bat build
```

## Operations doc

- 운영 env / logging 정책, health check, 배포 후 smoke check 기준은 [C:\bangpot\backend\docs\operations\backend-common-ops-round-01.md](C:\bangpot\backend\docs\operations\backend-common-ops-round-01.md)에 정리되어 있습니다.
- Slack 운영 알림 정책, appender 구조, payload 필드, 제외 이벤트 기준은 [C:\bangpot\backend\docs\operations\backend-common-ops-round-02.md](C:\bangpot\backend\docs\operations\backend-common-ops-round-02.md)에 정리되어 있습니다.
