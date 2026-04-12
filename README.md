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

1. 로컬 PostgreSQL 접속 정보, JWT, Kakao OAuth, frontend base URL, required terms version 값을 `src/main/resources/application-local-secret.yml`에 작성합니다.
2. Kakao developer console에 `http://localhost:8080/login/oauth2/code/kakao`를 등록합니다.
3. 기본 `local` profile로 애플리케이션을 실행합니다.

`local` profile은 `src/main/resources/application-local-secret.yml`을 직접 읽습니다.

### PowerShell example

```powershell
./gradlew.bat bootRun
```

## Profiles

- `local`: `src/main/resources/application-local-secret.yml`만 읽고, SQL logging을 켜고, `ddl-auto=update`, JWT secure cookie 기본값은 `false`를 사용합니다.
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
  - `3초 이상` 느린 요청 경고 로그
  - 요청 처리 중 `5xx` 서버 오류 로그
  - 인증/권한 치명 이벤트 로그
- `error-log` 채널
  - 모든 `ERROR` 로그
  - 운영 서버 startup 실패
  - startup 직후 데이터베이스 확인 실패

현재 Slack 대상 이벤트는 아래 범위로 제한합니다.

- `request.slow`
  - `durationMs >= 3000`
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
- `GET /api/users/me`
- `PATCH /api/users/me`
- `GET /api/users/nickname-availability`
- `POST /api/auth/complete`
- `POST /api/auth/logout`
- `POST /api/crews`
- `GET /api/crews/public`
- `GET /api/crews/{crewId}`
- `PATCH /api/crews/{crewId}/visibility`
- `GET /api/crews/{crewId}/members`
- `GET /api/crews/{crewId}/policies`
- `GET /api/crews/{crewId}/join`
- `GET /api/crews/{crewId}/invite-candidates`
- `POST /api/crews/{crewId}/invites`
- `GET /api/crew-invites/me`
- `POST /api/crew-invites/{inviteId}/accept`
- `POST /api/crew-invites/{inviteId}/reject`
- `GET /api/crews/{crewId}/join-requests`
- `POST /api/crews/{crewId}/join-requests`
- `GET /api/crews/{crewId}/join-requests/pending`
- `POST /api/crews/{crewId}/join-requests/{requestId}/approve`
- `POST /api/crews/{crewId}/join-requests/{requestId}/reject`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /oauth2/authorization/kakao`
- `GET /login/oauth2/code/kakao`

`GET /api/crews/public` and `GET /api/crews/{crewId}/join` are public read endpoints and do not require authentication.
`GET /api/crews/{crewId}` is the internal crew hub endpoint and is available only to joined crew members; it returns the crew summary, current user's role, `hasNotice`, and leader-only `pendingJoinRequestCount`.
`PATCH /api/crews/{crewId}/visibility` is the leader-only crew visibility toggle endpoint; it updates only the current `visibility` (`PUBLIC` or `PRIVATE`) and immediately affects new explore exposure and direct join request availability while leaving existing pending join requests untouched.
`GET /api/crews/{crewId}/members` is the joined-member-only crew members list endpoint; it returns leader-first ordering and then remaining members by `joinedAt desc`, while currently unsupported profile fields use safe defaults (`profileImageUrl=null`, `bio=null`, `gender=null`, `escapeCount=0`).
`GET /api/crews/{crewId}/policies` is the joined-member-only crew policy read endpoint; it returns `{policyId,title,content}` records and responds with `200 OK` plus `[]` when no policy exists.
`GET /api/crews/{crewId}/join-requests/pending` is the leader main-page summary endpoint, and `GET /api/crews/{crewId}/join-requests` is the leader management endpoint with message and status included.
`GET /api/crews/{crewId}/invite-candidates` and `POST /api/crews/{crewId}/invites` are private-crew leader endpoints for direct invite flow; candidate list excludes existing members, already pending invite targets, and the leader themself.
`GET /api/crew-invites/me` returns the current full user's invite history, and `POST /api/crew-invites/{inviteId}/accept|reject` process only `PENDING` invites while keeping invite rows as status history.
Profile and nickname availability moved to `/api/users/...`; frontend consumers should stop calling legacy `/api/auth/profile` and `/api/auth/nickname-availability`.

## Common API error contract

Backend API는 성공 응답을 별도 envelope로 감싸지 않고 resource JSON을 그대로 반환합니다.
대신 실패 응답은 전역 공통 JSON 계약을 사용합니다.

### Failure response shape

```json
{
  "code": "COMMON_VALIDATION_ERROR",
  "message": "입력값이 올바르지 않습니다.",
  "requestId": "req-123",
  "fieldErrors": [
    {
      "field": "nickname",
      "message": "닉네임은 비어 있을 수 없습니다."
    }
  ]
}
```

### Rules

- 최소 필드는 `code`, `message`, `requestId`, `fieldErrors` 입니다.
- HTTP status는 body가 아니라 transport 수준에서 확인합니다.
- validation 실패가 아니면 `fieldErrors`는 빈 배열입니다.
- 현재 공통 계약은 `auth/common security` 경로에 먼저 반영돼 있습니다.
- `requestId`는 `X-Request-Id` 추적 기준과 연결됩니다.

### Current code groups

- `COMMON_*`
  - `COMMON_VALIDATION_ERROR`
  - `COMMON_INTERNAL_ERROR`
- `AUTH_*`
  - `AUTH_UNAUTHENTICATED`
  - `AUTH_ACCESS_DENIED`
  - `AUTH_INVALID_NICKNAME`
  - `AUTH_REQUIRED_TERMS_AGREEMENT`
  - `AUTH_DUPLICATE_NICKNAME`
  - `AUTH_USER_NOT_FOUND`
  - `AUTH_COMPLETION_NOT_ALLOWED`

## Verification commands

```powershell
./gradlew.bat lint
./gradlew.bat test
./gradlew.bat build
```

## Production deployment

- 운영 배포는 GitHub Actions workflow [C:\bangpot\backend\.github\workflows\deploy-prod.yml](C:/bangpot/backend/.github/workflows/deploy-prod.yml) 기준으로 수행합니다.
- `prod` 브랜치 push 또는 `workflow_dispatch`로 배포를 실행합니다.
- workflow는 EC2에 소스를 동기화하고, `.env`를 생성한 뒤, `docker compose`로 backend와 PostgreSQL을 함께 기동합니다.
- 배포 직후 아래를 자동 확인합니다.
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
  - `/api/auth/me`
  - startup 로그
  - smoke check requestId 로그

## Operations doc

- 운영 env / logging 정책, health check, 배포 후 smoke check 기준은 [C:\bangpot\workdocs-repo\docs\plans\results\common-ops\00-common-ops-backend-round-01-result.md](C:\bangpot\workdocs-repo\docs\plans\results\common-ops\00-common-ops-backend-round-01-result.md)와 관련 handoff 문서에서 확인합니다.
- Slack 운영 알림 정책, appender 구조, payload 필드, 제외 이벤트 기준은 [C:\bangpot\workdocs-repo\docs\plans\results\common-ops\00-common-ops-backend-round-02-result.md](C:\bangpot\workdocs-repo\docs\plans\results\common-ops\00-common-ops-backend-round-02-result.md)와 관련 handoff 문서에서 확인합니다.
- 공통 실패 응답 계약, `COMMON_* / AUTH_*` 코드, `auth/common security` 적용 범위는 [C:\bangpot\workdocs-repo\docs\plans\results\common-error\00-common-error-contract-backend-round-01-result.md](C:\bangpot\workdocs-repo\docs\plans\results\common-error\00-common-error-contract-backend-round-01-result.md)와 관련 handoff 문서에서 확인합니다.
