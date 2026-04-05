# BangPot Backend

BangPot의 auth 기능과 공통 운영 준비 기준을 관리하는 backend 저장소입니다.

## Stack

- Java 21
- Spring Boot
- Spring Security OAuth2 Client
- Spring Boot Actuator
- Gradle
- PostgreSQL

## Local setup

1. `.env.example`을 `.env`로 복사합니다.
2. 로컬 PostgreSQL 연결 정보, JWT, Kakao OAuth, frontend base URL, required terms version 값을 채웁니다.
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

- `local`: 로컬 `.env`와 optional `src/main/resources/application-local-secret.yml`을 함께 읽고, SQL logging을 켜며, `ddl-auto=update`, JWT secure cookie 기본값은 `false`입니다.
- `prod`: environment variable 기반 datasource / auth 설정을 사용하고, SQL logging을 끄며, `ddl-auto=validate`, JWT secure cookie는 운영 기준값을 따릅니다.

## Operational logging

- 모든 요청은 `X-Request-Id`를 재사용하거나 새로 발급받고, 응답 헤더에도 같은 값을 내려주며, `method`, `path`, `status`, `durationMs`를 포함한 completion log를 남깁니다.
- `local`은 개발 편의를 위해 `com.bangpot=DEBUG`와 SQL logging을 유지합니다.
- `prod`는 운영 로그를 짧고 읽기 쉽게 유지하기 위해 `INFO` 기본값과 SQL logging off 기준을 사용합니다.
- Console log는 `timestamp`, `level`, `service`, `env`, `requestId`, `logger`, `message`를 공통 포맷으로 사용합니다.
- request log와 auth audit log에는 raw token, cookie, secret, request body 값을 남기지 않습니다.

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

운영 env 키, logging 정책, health check, 배포 후 smoke check 기준은 [docs/operations/backend-common-ops-round-01.md](C:\bangpot\backend\docs\operations\backend-common-ops-round-01.md)에 정리되어 있습니다.
