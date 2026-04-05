# Backend Common Ops Round 01 Report

작성일: 2026-04-04

## 1. 작업 개요

이번 라운드는 backend 저장소 안에서 즉시 구현하고 검증할 수 있는 Common 운영 기반을 고정하는 데 집중했다.

- 운영 설정 기준을 `local` / `prod` 2개 환경으로 정리했다.
- 로컬 편의 설정과 운영 secret 기준을 분리했다.
- 공통 요청 추적, auth/security 핵심 이벤트 로그, health check, smoke check 문서를 반영했다.
- 도메인 기능 확장이나 Slack 운영 알림, 공통 배포/롤백 체크리스트 정리는 이번 라운드 범위에서 제외했다.

## 2. 실제 변경 파일

### 코드 / 설정

- `.env.example`
- `.gitignore`
- `src/main/java/com/bangpot/auth/application/service/CompleteTempUserService.java`
- `src/main/java/com/bangpot/auth/application/service/LoginWithProviderService.java`
- `src/main/java/com/bangpot/auth/application/service/LogoutService.java`
- `src/main/java/com/bangpot/auth/application/usecase/LogoutUseCase.java`
- `src/main/java/com/bangpot/auth/infrastructure/config/AuthFrontendProperties.java`
- `src/main/java/com/bangpot/auth/infrastructure/config/AuthJwtProperties.java`
- `src/main/java/com/bangpot/auth/infrastructure/logging/AuthAuditLogger.java`
- `src/main/java/com/bangpot/auth/infrastructure/oauth/KakaoOAuth2AuthenticationFailureHandler.java`
- `src/main/java/com/bangpot/auth/presentation/AuthCookieFactory.java`
- `src/main/java/com/bangpot/auth/presentation/AuthLogoutController.java`
- `src/main/java/com/bangpot/common/config/SecurityConfig.java`
- `src/main/java/com/bangpot/common/logging/RequestTrace.java`
- `src/main/java/com/bangpot/common/logging/RequestTracingFilter.java`
- `src/main/java/com/bangpot/common/security/LoggingAccessDeniedHandler.java`
- `src/main/java/com/bangpot/common/security/LoggingAuthenticationEntryPoint.java`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/application.yml`
- `src/main/resources/logback-spring.xml`

### 테스트

- `src/test/java/com/bangpot/auth/application/AuthUseCaseServicesTest.java`
- `src/test/java/com/bangpot/auth/application/LogoutServiceTest.java`
- `src/test/java/com/bangpot/auth/infrastructure/logging/AuthAuditLoggerTest.java`
- `src/test/java/com/bangpot/auth/infrastructure/oauth/KakaoOAuth2AuthenticationFailureHandlerTest.java`
- `src/test/java/com/bangpot/auth/presentation/AuthLogoutControllerTest.java`
- `src/test/java/com/bangpot/common/logging/RequestTracingFilterTest.java`
- `src/test/java/com/bangpot/common/security/SecurityFailureHandlersTest.java`
- `src/test/java/com/bangpot/health/presentation/ActuatorHealthEndpointTest.java`
- `src/test/resources/application-test.yml`

### 문서

- `README.md`
- `docs/operations/backend-common-ops-round-01.md`

### 제거한 파일

- `src/main/java/com/bangpot/health/presentation/HealthController.java`
- `src/main/java/com/bangpot/health/presentation/package-info.java`
- `src/test/java/com/bangpot/health/presentation/HealthControllerTest.java`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-local-secret.yml.example`

## 3. 구현 요약

### 3.1 env / secret 기준

- 환경 기준은 `local` / `prod` 2개만 유지했다.
- `local`은 `application-local.yml`, optional `.env`, optional `src/main/resources/application-local-secret.yml`을 사용한다.
- `prod`는 저장소 내부 convenience 파일 로딩 없이 environment variable 기준으로만 동작하도록 정리했다.
- 운영 env 키 문서를 `README.md`와 `docs/operations/backend-common-ops-round-01.md`에 반영했다.

### 3.2 로그 / 민감정보 마스킹 기준

- `local`은 `DEBUG` 중심, SQL 로그 on 기준으로 유지했다.
- `prod`는 `INFO` 중심, SQL 로그 off 기준으로 분리했다.
- 콘솔 로그 포맷은 최소 공통 필드 기준으로 고정했다.
  - `timestamp`
  - `level`
  - `service`
  - `env`
  - `requestId`
  - `logger`
  - `message`
- 구조 키는 영어로 유지하고, 사람이 읽는 `message` 값은 한글로 넣었다.
- raw `Authorization` header, JWT, cookie 값, secret 값, request body, query string은 운영 로그에 남기지 않도록 제한했다.

### 3.3 requestId 기준

- 요청 헤더 `X-Request-Id`가 오면 그대로 재사용한다.
- 없으면 backend가 새 UUID를 생성한다.
- 최종 requestId는 응답 헤더 `X-Request-Id`로 다시 내려준다.
- 같은 requestId를 공통 요청 로그와 auth/security 감사 로그에 함께 사용한다.
- 이 기준은 request가 controller까지 도달하지 않는 경우까지 포함해야 해서 interceptor가 아니라 filter에 반영했다.

### 3.4 auth / security 핵심 이벤트 로그

이번 라운드에서 반영한 이벤트는 아래와 같다.

- `auth.login.success`
- `auth.login.failure`
- `auth.logout`
- `auth.protected_resource_access_failed`
- `auth.access_denied`
- `auth.state.changed`

로그에는 requestId와 최소 운영 판단 필드만 포함한다.

- 로그인 성공: provider, userId, authStatus, completionRequired
- 로그인 실패: provider, path, failureType
- 로그아웃: userId
- 보호 자원 접근 실패: method, path
- 권한 거부: userId, method, path
- 인증 상태 전환: userId, previousStatus, currentStatus, reason

### 3.5 health / smoke 기준

- health check는 actuator 기준으로만 정리했다.
- `GET /actuator/health/liveness`
  - 서버 프로세스 기동 확인
- `GET /actuator/health/readiness`
  - DB 연결 포함 readiness 확인
- 중복 성격의 `/api/health`는 제거했다.

smoke check는 배포 직후 backend 상태를 짧게 확인하는 절차까지만 다뤘다.

- `liveness` 확인
- `readiness` 확인
- `X-Request-Id`를 포함한 `/api/auth/me` 응답 확인

## 4. handoff 기준 대비 완료 / 미완료

### 완료

- `local` / `prod` 운영 설정 기준 정리
- 운영 secret과 로컬 편의 설정 분리
- 운영 env 키 문서화
- `local` / `prod` 로그 레벨 분리
- 최소 운영 로그 포맷 반영
- 민감정보 마스킹 기준 반영
- requestId 생성 / 전달 반영
- 인증 / 권한 핵심 이벤트 로그 반영
- backend health check 기준 반영
- backend 배포 후 smoke check 기준 문서화
- README 및 운영 문서 갱신

### 미완료 또는 의도적 제외

- Slack 운영 알림 정책 및 연동
- 공통 배포 / 롤백 체크리스트
- 외부 로그 수집 플랫폼 연동
- 메트릭 / 대시보드 구축
- 분산 트레이싱 시스템
- 도메인 기능 확장

위 항목들은 handoff와 제외 범위 기준에 따라 이번 라운드에서 닫지 않았다.

## 5. smoke check 실패 시 남긴 메모

smoke check 실패 시 backend 저장소 기준으로 아래 순서로 확인하도록 정리했다.

- `liveness` 실패
  - 누락 env 키
  - config binding 실패
  - startup exception
- `readiness` 실패
  - `BANGPOT_DB_*` 값
  - PostgreSQL reachability
  - schema validation 실패
- `/api/auth/me` 실패 또는 `X-Request-Id` echo 실패
  - request tracing filter 동작
  - auth/security 감사 로그
  - JWT cookie 설정
  - security filter wiring

이 메모는 backend-only 다음 행동 메모이며, 공통 배포 / 롤백 판단 기준으로 확장하지 않는다.

## 6. 실행한 검증

### 자동 검증

아래 명령을 실행했고 모두 통과했다.

```powershell
./gradlew.bat lint test build
```

추가로 주요 범위 테스트를 개별 또는 묶음으로 실행했다.

```powershell
./gradlew.bat test --tests com.bangpot.auth.application.AuthUseCaseServicesTest --tests com.bangpot.auth.infrastructure.logging.AuthAuditLoggerTest --tests com.bangpot.common.logging.RequestTracingFilterTest --tests com.bangpot.common.security.SecurityFailureHandlersTest --tests com.bangpot.health.presentation.ActuatorHealthEndpointTest --tests com.bangpot.auth.infrastructure.oauth.KakaoOAuth2AuthenticationFailureHandlerTest --tests com.bangpot.auth.presentation.AuthLogoutControllerTest
```

### 수동 로컬 확인

수동 확인에서 아래 동작을 재현했다.

- `GET /actuator/health/liveness`
  - `200 OK`
- `GET /actuator/health/readiness`
  - `200 OK`
- `GET /api/auth/me`
  - requestId 자동 생성 응답 확인
- `GET /api/auth/me` with `X-Request-Id: local-smoke-001`
  - 같은 requestId echo 확인
- `POST /api/auth/logout`
  - `204 No Content`
  - `Set-Cookie: access_token=; Max-Age=0 ...`
- 보호 자원 접근 실패 요청
  - `401`
  - `auth.protected_resource_access_failed`
  - `request.completed status=401`

## 7. 남은 리스크 / 문서 공백

- requestId 이름과 형식은 문서상 완전히 닫히지 않아 이번 라운드에서는 `X-Request-Id` 최소 기준으로만 반영했다.
- auth/security 외 일반 도메인 이벤트 로그는 이번 라운드 범위에 포함하지 않았다.
- local secret 기준은 현재 `src/main/resources/application-local-secret.yml` 경로를 사용한다. local 설정 키가 추가될 경우 이 파일 기준으로만 관리해야 한다.
- workspace 기준으로 root `application-local-secret.yml.example` 파일이 남아 있으면 최종 정리 전에 제외 여부를 다시 확인할 필요가 있다.

## 8. 결론

이번 라운드의 stop line 기준은 충족했다.

- backend 운영 env 키와 secret 기준이 local 설정과 분리됐다.
- `prod` 로그 레벨과 포맷 기준이 반영됐다.
- requestId로 요청 로그를 묶을 수 있다.
- auth/security 핵심 이벤트 로그가 남는다.
- health check와 backend smoke check 기준이 코드와 문서에서 재현 가능하다.
- smoke check 실패 시 backend 기준 다음 행동 메모가 있다.

공통 배포 / 롤백 체크리스트는 이번 라운드 범위 밖으로 유지했다.
