# Backend Common Ops Round 02

Round 02는 Round 01에서 정리한 로그, requestId, health 기준 위에 Slack 최소 운영 알림을 붙이는 라운드다.
이번 라운드의 목적은 운영자가 로그를 직접 뒤지기 전에 이상 상황을 빠르게 인지하되, 스캔성 노이즈는 Slack으로 보내지 않는 최소 운영 구조를 고정하는 것이다.

## Slack 운영 알림 정책

현재 backend는 `all-log`와 `error-log` 두 종류의 Slack webhook을 사용한다.

- `all-log`
  - startup 완료 로그
  - `3초 이상` 느린 요청 경고 로그
  - 요청 처리 중 `5xx` 서버 오류 로그
  - 인증/권한 치명 이벤트 로그
- `error-log`
  - 모든 `ERROR` 로그
  - 운영 서버 startup 실패
  - startup 직후 데이터베이스 확인 실패

Slack 운영 알림은 애플리케이션 코드가 직접 webhook을 호출하는 방식이 아니라, `logback-spring.xml`에 연결된 `SlackWebhookAppender`가 로그를 받아 전송하는 방식으로 고정한다.

## Slack payload 최소 필드

Slack payload에는 아래 필드만 포함한다.

- `timestamp`
- `service`
- `environment`
- `requestId`
- `path` 또는 `context`
- `summary`

현재 구현은 운영 가독성을 위해 `reason` 필드를 추가로 포함한다.

payload는 `SlackWebhookAppender`가 로그 이벤트에서 필요한 값을 추출해 만든다.

- 요청 기반 로그는 `path`를 사용한다.
- startup 로그는 요청이 없으므로 `context`를 사용한다.
- request context가 없는 경우 `requestId`는 `na`로 남긴다.

## 실제 Slack 연동 대상 이벤트

현재 Slack으로 전달되는 대상은 아래와 같다.

- `request.slow`
  - `RequestTracingFilter`가 `durationMs >= 3000`이면 `WARN` 로그를 남긴다.
  - `all-log`에서 확인 가능하다.
- backend `5xx`
  - `ServerErrorLoggingFilter`가 `event=request.failed` `ERROR` 로그를 남긴다.
  - `all-log`와 `error-log` 모두 확인 가능하다.
- 운영 서버 startup 실패
  - Spring Boot startup 중 발생한 `ERROR` 로그를 `error-log`가 받는다.
- startup 직후 데이터베이스 확인 실패
  - `StartupLifecycleLogger`가 `event=application.startup.health_failed` `ERROR` 로그를 남긴다.
- `auth.login.failure`
  - `AuthAuditLogger`의 `WARN` 로그를 `all-log`가 받는다.
- `auth.access_denied`
  - `AuthAuditLogger`의 `WARN` 로그를 `all-log`가 받는다.

## 제외한 항목과 이유

아래 항목은 이번 라운드에서 Slack 대상에서 제외한다.

- 모든 일반 `4xx`
  - 노이즈가 많고 이번 라운드의 최소 범위를 넘는다.
- `auth.protected_resource_access_failed`
  - AWS 공개 환경에서 스캔성 `401` 요청이 반복적으로 발생할 수 있다.
  - 개별 Slack 알림으로 보내면 운영 피로도가 높아진다.
  - 대신 `AuthAuditLogger`와 `request.completed` 로그, `requestId`, `path` 기준으로 추적한다.
- `auth.login.success`, `auth.logout`, `auth.state.changed`
  - 실패나 치명 상황이 아니므로 Slack 최소 운영 알림 대상에서 제외한다.
- threshold, dedupe, 채널 분리 자동화
  - 문서에 닫힌 정책이 없으므로 이번 라운드에서 확정하지 않는다.

## appender 기준

`SlackWebhookAppender`는 아래 규칙으로 Slack 대상을 판단한다.

- `ERROR` 로그는 모두 Slack 대상이다.
- `WARN` 로그는 Slack 대상이다.
- 단, `event=auth.protected_resource_access_failed`는 scanner 노이즈로 보고 Slack에서 제외한다.
- `INFO` 로그는 `StartupLifecycleLogger`만 Slack 대상으로 허용한다.

즉, “어떤 상황을 Slack으로 보낼지”를 애플리케이션 이벤트 리스너나 서비스 호출로 분산하지 않고, 운영 로그 규칙과 logback appender 기준으로 고정한다.

## 운영 서버 기동 확인 기준

이번 라운드에서 운영자가 알고 싶어 하는 핵심은 아래 두 가지다.

1. 운영 서버가 정상적으로 올라왔는가
2. 올라오지 못했다면 왜 실패했는가

이를 위해 아래 기준을 사용한다.

- startup 성공
  - `StartupLifecycleLogger`
  - `event=application.startup.completed`
- startup 직후 데이터베이스 확인 실패
  - `StartupLifecycleLogger`
  - `event=application.startup.health_failed`
- startup 실패
  - Spring Boot startup 중 발생하는 `ERROR` 로그
  - `error-log` 채널에서 바로 확인

## 민감정보 비노출 기준

Slack payload와 appender는 아래 값을 그대로 보내지 않는다.

- raw token
- cookie 값
- secret 값
- query string
- request body
- 개인정보 raw 값

구현 기준은 아래와 같다.

- `path`는 query string을 제거한 path-only 값만 사용한다.
- `authorization`, `password`, `access_token`, `refresh_token`, `cookie`, `secret` 패턴은 `[REDACTED]`로 마스킹한다.
- startup 실패와 startup health 실패는 raw exception message 대신 예외 타입 기준으로만 요약한다.

## 스캔성 요청 확인 기준

스캔성 요청은 Slack으로 보내지 않지만, 로그와 문서 기준으로는 확인 가능해야 한다.

확인 대상 로그:

- `event=auth.protected_resource_access_failed`
- `event=request.completed` with `status=401`

운영자는 아래 필드로 추적한다.

- `timestamp`
- `requestId`
- `path`
- `status`

이번 라운드는 “사후 확인 가능”까지만 닫고, threshold 기반 이상 감지나 즉시 알림은 다음 과제로 넘긴다.

## 운영 설정 키

### prod

- `BANGPOT_OPS_SLACK_ALL_LOG_WEBHOOK_URL`
- `BANGPOT_OPS_SLACK_ERROR_LOG_WEBHOOK_URL`

### local

`src/main/resources/application-local-secret.yml`에 아래 키를 추가해 직접 확인한다.

```yml
bangpot:
  ops:
    slack:
      all-log-webhook-url: https://hooks.slack.com/services/...
      error-log-webhook-url: https://hooks.slack.com/services/...
```

## 검증 기준

이번 라운드에서 확인해야 하는 항목은 아래와 같다.

- `SlackWebhookAppender`가 startup `INFO`, auth `WARN`, server `ERROR`를 구분해 전송 대상 판단을 한다.
- `ServerErrorLoggingFilter`가 요청 처리 중 `5xx`를 `ERROR` 로그로 남긴다.
- `StartupLifecycleLogger`가 startup 완료와 startup 직후 데이터베이스 확인 실패를 로그로 남긴다.
- `auth.login.failure`, `auth.access_denied`는 Slack 대상 로그로 유지된다.
- Slack payload에 최소 추적 정보가 들어간다.
- token/cookie/secret/query string/raw 개인정보는 Slack payload에 그대로 노출되지 않는다.

이번 라운드는 최소 Slack 운영 알림 정책을 코드와 문서에 고정하는 지점에서 멈춘다.
노이즈 최적화, threshold 기반 이상 감지, 채널 분리 자동화는 다음 과제로 넘긴다.
