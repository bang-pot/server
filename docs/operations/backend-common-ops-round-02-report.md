# Backend Common Ops Round 02 Report

## 1. 문서 목적
이 문서는 `backend-builder`의 Common Ops Round 02 작업 완료 보고를 backend 저장소 기준으로 정리한 결과 문서다.
Round 02 handoff 기준으로 실제 Slack 연동 대상, 제외한 항목, 민감정보 비노출 기준, 검증 결과를 빠르게 확인할 수 있게 한다.

## 2. 기준 문서
### Workdocs
- `docs/plans/common-ops/00-common-ops-development-plan.md`
- `docs/plans/common-ops/00-common-ops-backend-round-02-handoff.md`

### 참고 결과 문서
- `docs/plans/results/common-ops/00-common-ops-backend-round-01-result.md`
- `docs/plans/results/common-ops/00-common-ops-frontend-round-01-result.md`

## 3. 이번 라운드에서 고정한 기준
- Slack 운영 알림은 애플리케이션 코드가 직접 webhook을 호출하지 않고 `logback-spring.xml`과 `SlackWebhookAppender` 기준으로 처리한다.
- Slack webhook은 `all-log`와 `error-log` 두 채널로 분리한다.
- `all-log`는 startup 완료, 구조화된 `5xx`, 인증/권한 치명 이벤트를 본다.
- `error-log`는 모든 `ERROR` 로그를 본다.
- 스캔성 `401`은 Slack에서 제외하고 로그와 문서 기준으로만 확인한다.

## 4. 실제 Slack 연동 대상 이벤트 목록
- `application.startup.completed`
  - startup 완료 `INFO`
  - `all-log`
- `application.startup.health_failed`
  - startup 직후 데이터베이스 확인 실패 `ERROR`
  - `all-log`, `error-log`
- Spring Boot startup failure `ERROR`
  - 예: `Port 8080 was already in use`
  - `error-log`
- `request.failed`
  - 요청 처리 중 `5xx`
  - `all-log`, `error-log`
- `auth.login.failure`
  - `WARN`
  - `all-log`
- `auth.access_denied`
  - `WARN`
  - `all-log`

## 5. payload에 담긴 필드 목록
Slack 본문에는 아래 필드를 사용한다.

- `timestamp`
- `service`
- `environment`
- `requestId`
- `path` 또는 `context`
- `reason`
- `summary`

메모:
- Round 02 최소 기준 필드는 `timestamp`, `service`, `environment`, `requestId`, `path/context`, `summary`다.
- 현재 구현은 운영 가독성을 위해 `reason`을 추가로 포함한다.

## 6. 제외한 항목과 이유
- 모든 일반 `4xx`
  - 운영 노이즈가 많고 이번 라운드 최소 범위를 넘는다.
- `auth.protected_resource_access_failed`
  - AWS 공개 환경의 스캔성 `401` 요청이 많아 개별 Slack 알림 대상으로 두지 않는다.
  - 대신 `AuthAuditLogger`, `request.completed`, `requestId`, `path` 기준으로 추적한다.
- `auth.login.success`, `auth.logout`, `auth.state.changed`
  - 실패나 치명 상황이 아니므로 Slack 최소 운영 알림 대상에서 제외한다.
- threshold, dedupe, 채널 분리 자동화
  - 문서에 닫힌 기준이 없어 이번 라운드에서 확정하지 않는다.

## 7. 민감정보 비노출 검증 결과
- Slack payload에는 raw token, cookie, secret, query string, request body, 개인정보 raw 값을 포함하지 않는다.
- `path`는 query string 제거 후 path-only 값만 사용한다.
- `authorization`, `password`, `access_token`, `refresh_token`, `cookie`, `secret` 패턴은 `[REDACTED]`로 마스킹한다.
- startup 실패는 긴 원문 전체를 그대로 보내지 않고 짧은 `summary`와 분리된 `reason`으로 보낸다.
- `SlackWebhookAppenderTest`에서 민감정보 마스킹과 scanner `401` 제외 동작을 검증했다.

## 8. 실제 변경 파일 목록
### 문서
- `README.md`
- `docs/operations/backend-common-ops-round-02.md`
- `docs/operations/backend-common-ops-round-02-report.md`

### main
- `src/main/java/com/bangpot/BangpotBackendApplication.java`
- `src/main/java/com/bangpot/auth/infrastructure/logging/AuthAuditLogger.java`
- `src/main/java/com/bangpot/auth/infrastructure/oauth/KakaoOAuth2AuthenticationFailureHandler.java`
- `src/main/java/com/bangpot/auth/presentation/AuthCookieFactory.java`
- `src/main/java/com/bangpot/common/logging/RequestTracingFilter.java`
- `src/main/java/com/bangpot/common/logging/ServerErrorLoggingFilter.java`
- `src/main/java/com/bangpot/common/logging/StartupLifecycleLogger.java`
- `src/main/java/com/bangpot/common/ops/slack/SlackWebhookAppender.java`
- `src/main/java/com/bangpot/common/security/LoggingAccessDeniedHandler.java`
- `src/main/java/com/bangpot/common/security/LoggingAuthenticationEntryPoint.java`
- `src/main/resources/application.yml`
- `src/main/resources/logback-spring.xml`

### test
- `src/test/java/com/bangpot/auth/presentation/AuthControllerTest.java`
- `src/test/java/com/bangpot/common/logging/ServerErrorLoggingFilterTest.java`
- `src/test/java/com/bangpot/common/logging/StartupLifecycleLoggerTest.java`
- `src/test/java/com/bangpot/common/ops/slack/SlackWebhookAppenderTest.java`
- `src/test/resources/application-test.yml`

## 9. 실행한 테스트 / 검증 명령과 결과
### 자동 검증
```powershell
./gradlew.bat test --tests com.bangpot.common.ops.slack.SlackWebhookAppenderTest --tests com.bangpot.common.logging.StartupLifecycleLoggerTest --tests com.bangpot.common.logging.ServerErrorLoggingFilterTest --tests com.bangpot.auth.infrastructure.oauth.KakaoOAuth2AuthenticationFailureHandlerTest --tests com.bangpot.common.security.SecurityFailureHandlersTest --tests com.bangpot.auth.presentation.AuthControllerTest --tests com.bangpot.auth.presentation.AuthLogoutControllerTest
```
- 결과: `BUILD SUCCESSFUL`

```powershell
./gradlew.bat lint test build
```
- 결과: `BUILD SUCCESSFUL`

### 수동 확인
- startup 성공
  - `application.startup.completed`
  - `all-log` 수신 확인
- startup 실패
  - 포트 충돌(`8080 already in use`) 재현
  - `error-log` 수신 확인
- scanner `401`
  - `auth.protected_resource_access_failed`
  - 로그에는 남고 Slack에는 가지 않음을 확인
- DB 중단 후 `5xx`
  - `request.failed`
  - `all-log`, `error-log` 수신 확인

## 10. handoff 대비 완료 / 미완료
### 완료
- Slack 최소 운영 알림 정책 고정
- Slack payload 최소 필드 반영
- backend `5xx` Slack 연결
- 배포 직후 health 실패 Slack 연결
- 인증/권한 치명 이벤트 Slack 연결
- 민감정보 비노출 검증
- README / 운영 문서 갱신

### 미완료
- 없음

메모:
- `auth.access_denied`는 자동 테스트 기준으로 검증했고, 현재 코드에는 자연스러운 수동 `403` 재현 경로가 없어 운영 기준과 테스트 코드로 확인한다.

## 11. 남은 리스크와 다음 후보
- scanner `401`은 Slack에 보내지 않으므로, 이상 징후 급증 감지는 다음 라운드에서 threshold 기반으로 다뤄야 한다.
- `error-log`는 Hikari, JDBC, Servlet 같은 프레임워크 `ERROR`도 함께 받으므로, 이후 운영 리허설에서 노이즈 조정이 필요할 수 있다.
- 채널 분리 자동화, dedupe, threshold 정책은 이번 라운드에서 닫지 않았다.
