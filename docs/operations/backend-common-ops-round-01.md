# Backend Common Ops Round 01

Round 01 fixes the backend-only operational baseline inside this repository. It does not add Slack alerts, dashboards, distributed tracing, or common rollback policy.

## Profile and secret rules

| Profile | Config source | Secret rule | Logging default | DB rule |
| --- | --- | --- | --- | --- |
| `local` | `application-local.yml`, optional `.env`, optional `src/main/resources/application-local-secret.yml` | Local-only secret file is loaded from classpath resources | `com.bangpot=DEBUG`, SQL logging on | `ddl-auto=update` |
| `prod` | `application-prod.yml` + environment variables | No local convenience file loading | `INFO`, SQL logging off | `ddl-auto=validate` |

Rules:

- `.env` is local-only convenience and is not the source of truth for `prod`.
- `src/main/resources/application-local-secret.yml` is the local-only secret file currently loaded by the `local` profile.
- `prod` must provide operational secrets through environment variables or deployment secret management outside this repository.

## Operational env keys

Required for `prod` unless marked optional:

| Key | Purpose | local | prod |
| --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects `local` or `prod` startup mode | optional | required |
| `BANGPOT_DB_URL` | PostgreSQL JDBC URL | optional | required |
| `BANGPOT_DB_USERNAME` | PostgreSQL username | optional | required |
| `BANGPOT_DB_PASSWORD` | PostgreSQL password | optional | required |
| `KAKAO_CLIENT_ID` | Kakao OAuth client ID | optional | required |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth client secret | optional | required |
| `KAKAO_REDIRECT_URI` | Kakao OAuth redirect URI | optional | required |
| `BANGPOT_AUTH_FRONTEND_BASE_URL` | Frontend redirect base URL | optional | required |
| `BANGPOT_AUTH_JWT_SECRET` | JWT signing secret | optional | required |
| `BANGPOT_AUTH_REQUIRED_TERMS_VERSION` | Required terms version returned to clients | optional | required |
| `BANGPOT_AUTH_JWT_ISSUER` | JWT issuer label | optional | optional |
| `BANGPOT_AUTH_JWT_COOKIE_NAME` | Access token cookie name | optional | optional |
| `BANGPOT_AUTH_JWT_ACCESS_TOKEN_VALIDITY_SECONDS` | JWT lifetime in seconds | optional | optional |
| `BANGPOT_AUTH_JWT_SECURE_COOKIE` | Secure-cookie override | optional | optional |
| `KAKAO_CLIENT_AUTHENTICATION_METHOD` | OAuth client auth method | optional | optional |
| `SERVER_PORT` | Server port override | optional | optional |

## Logging baseline

Console logs use this minimum operational shape:

- `timestamp`
- `level`
- `service`
- `env`
- `requestId`
- `logger`
- `message`

Request tracing rule:

- Reuse inbound `X-Request-Id` when it exists.
- Generate a new request ID when it does not.
- Echo the chosen value back in the `X-Request-Id` response header.
- Use the same request ID across request completion logs and auth/security audit logs.

Auth/security audit events covered in this round:

- `auth.login.success`
- `auth.login.failure`
- `auth.logout`
- `auth.protected_resource_access_failed`
- `auth.access_denied`
- `auth.state.changed` for `TEMP -> FULL`

Sensitive-data rule:

- Do not log raw `Authorization` headers, JWT values, cookie values, secret values, request bodies, or query strings.
- Request completion logs record only method, request URI path, status, and duration.
- Auth audit logs record provider, internal user ID, auth status, and reason only.

## Health checks

Use these endpoints:

- `GET /actuator/health/liveness`
  - Purpose: confirm the backend process is running
  - Success: `200 OK` with `{"status":"UP"}`
- `GET /actuator/health/readiness`
  - Purpose: confirm the backend can reach the database
  - Success: `200 OK` with overall `UP` and `components.db.status=UP`

The liveness and readiness checks are intentionally separate so process-up and DB-up states do not get conflated.

## Post-deploy smoke check

Run these checks immediately after a backend deploy:

1. `curl -i https://<backend-host>/actuator/health/liveness`
   - Expect `200 OK`
   - Expect body status `UP`
2. `curl -i https://<backend-host>/actuator/health/readiness`
   - Expect `200 OK`
   - Expect overall status `UP`
   - Expect `components.db.status=UP`
3. `curl -i -H "X-Request-Id: smoke-backend-001" https://<backend-host>/api/auth/me`
   - Expect `200 OK`
   - Expect `X-Request-Id: smoke-backend-001` in the response headers
   - Expect guest-shaped auth response when no login cookie is present

This smoke check is backend-only. It is not a deploy or rollback checklist.

## Smoke failure next-action memo

If smoke check fails, use this backend-only memo before escalating:

- Liveness failed:
  - Check application startup logs for missing env keys, config binding failures, or boot exceptions.
- Readiness failed:
  - Check `BANGPOT_DB_*` values, network reachability to PostgreSQL, and schema validation failures.
- `/api/auth/me` failed or `X-Request-Id` was not echoed:
  - Check request tracing logs, auth/security audit logs, JWT cookie settings, and security filter wiring.

Do not treat this memo as an automatic rollback rule. Common deployment and rollback policy remains out of scope for this round.
