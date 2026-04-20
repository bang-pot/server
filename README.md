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
- `GET /api/home`
- `GET /api/users/me`
- `GET /api/users/me/calendar`
- `GET /api/users/me/logs`
- `GET /api/users/me/created-meetings`
- `GET /api/users/me/joined-meetings`
- `GET /api/users/me/crews`
- `GET /api/users/me/pending-crews`
- `GET /api/users/me/withdrawal-check`
- `POST /api/users/me/withdrawal`
- `DELETE /api/users/me/pending-crews/{joinRequestId}`
- `PATCH /api/users/me`
- `GET /api/users/nickname-availability`
- `POST /api/auth/complete`
- `POST /api/auth/logout`
- `POST /api/crews`
- `GET /api/crews/public`
- `GET /api/explore/themes`
- `GET /api/explore/themes/{themeId}`
- `POST /api/themes/{themeId}/favorite`
- `DELETE /api/themes/{themeId}/favorite`
- `GET /api/explore/meeting-create/crews`
- `GET /api/explore/filters`
- `GET /api/archive/meetings`
- `POST /api/uploads/log-photos`
- `POST /api/meetings/{meetingId}/logs`
- `GET /api/meetings/{meetingId}/logs/me`
- `GET /api/crews/{crewId}/logs`
- `GET /api/crews/{crewId}/logs/{logId}`
- `GET /api/crews/{crewId}/gallery`
- `GET /api/crews/{crewId}/gallery/{meetingId}`
- `PATCH /api/logs/{logId}`
- `DELETE /api/crews/{crewId}/logs/{logId}`
- `GET /api/crews/{crewId}`
- `POST /api/crews/{crewId}/leave`
- `POST /api/crews/{crewId}/members/{targetUserId}/remove`
- `POST /api/crews/{crewId}/transfer-leadership`
- `POST /api/crews/{crewId}/delete`
- `PATCH /api/crews/{crewId}/visibility`
- `GET /api/crews/{crewId}/members`
- `GET /api/crews/{crewId}/policies`
- `POST /api/crews/{crewId}/meetings`
- `PATCH /api/crews/{crewId}/meetings/{meetingId}`
- `GET /api/crews/{crewId}/meetings`
- `GET /api/crews/{crewId}/meetings/{meetingId}`
- `POST /api/crews/{crewId}/meetings/{meetingId}/join`
- `DELETE /api/crews/{crewId}/meetings/{meetingId}/join`
- `POST /api/crews/{crewId}/meetings/{meetingId}/close-recruitment`
- `POST /api/crews/{crewId}/meetings/{meetingId}/reopen-recruitment`
- `POST /api/crews/{crewId}/meetings/{meetingId}/cancel`
- `POST /api/crews/{crewId}/meetings/{meetingId}/complete`
- `POST /api/crews/{crewId}/meetings/{meetingId}/result`
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
`GET /api/home` is the public main-home composition endpoint. It always succeeds for both guests and logged-in users and returns a single home-screen payload shaped as `{isLoggedIn, cta, myCrews, upcomingMeetings, publicCrewPreview, themeExplorePreview}`. `cta.canExplorePublicCrews` is always `true`, and `cta.canCreateCrew` is `true` only for completed logged-in users. For guests, `myCrews` and `upcomingMeetings` are returned as empty sections with `items=[]` and `totalCount=0` so the frontend can keep one stable layout. For logged-in users, `myCrews` returns up to five active crews ordered by `crewName asc`, then `crewId asc`, while `totalCount` reuses the same `ACTIVE membership + ACTIVE crew` rule as `GET /api/users/me`. `upcomingMeetings` returns up to five meetings ordered by `meetingDate asc`, `meetingTime asc`, then `meetingId asc`; it includes unfinished meetings (`RECRUITING`, `RECRUITMENT_CLOSED`) on active crews where the current user is the host or has participation status `JOINED`, `APPROVED`, or legacy-compatible `PENDING`. `publicCrewPreview` returns up to eight active public crews ordered by `crewId desc`, each with `crewId`, `crewName`, nullable `coverImageUrl`, active `memberCount`, and `isPublic=true`. `themeExplorePreview` reuses the existing public explore ordering (`themeId desc`) and returns up to eight cards with `themeId`, `themeName`, `storeName`, `regionName`, and nullable `thumbnailUrl`.
`GET /api/users/me` is the login-required my-page profile hub endpoint. It returns the current user's base profile fields plus the activity-entry counts needed by the frontend in one response: `id`, `nickname`, `profileImageUrl`, `createdMeetingsCount`, `joinedMeetingsCount`, `myCrewsCount`, and `pendingCrewsCount`. `profileImageUrl` is `null` in this round because user profile image persistence is not implemented yet. Count rules are fixed as follows: `createdMeetingsCount` includes every meeting where the current user is the host regardless of status (`RECRUITING`, `RECRUITMENT_CLOSED`, `COMPLETED`, `CANCELED`); `joinedMeetingsCount` includes only non-host meetings where the user has participation history with status `JOINED`, `PENDING`, or `APPROVED`, and excludes `LEFT`; `myCrewsCount` includes only current `ACTIVE` crew memberships on `ACTIVE` crews; `pendingCrewsCount` includes only the current user's `PENDING` join requests on `PUBLIC` and `ACTIVE` crews, and excludes private crews, deleted crews, approved rows, rejected rows, and canceled rows.
`GET /api/users/me/calendar` is the login-required profile calendar endpoint for the my-page detail screen. It returns `{items, totalCount}` without pagination because the calendar consumer needs one stable activity schedule array. Each item includes `meetingId`, `meetingTitle`, `crewId`, `crewName`, `date`, `time`, `meetingStatus`, `isCanceled`, and `participationRole`. The list includes meetings on `ACTIVE` crews where the current user is the host or has confirmed participation status `JOINED` or `APPROVED`; legacy `PENDING`, `LEFT`, and other inactive participation states are excluded because the calendar is meant to show confirmed schedules only. Included meeting statuses are `RECRUITING`, `RECRUITMENT_CLOSED`, `COMPLETED`, and `CANCELED`. `isCanceled` is `true` only when `meetingStatus = CANCELED`, so the frontend can render a gray canceled style and label without inferring it from the raw status string. Items are sorted by `date asc`, `time asc`, then `meetingId asc`.
`GET /api/users/me/logs` is the login-required profile activity detail endpoint for the "my meeting logs" entry on the my-page flow. It returns `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each item includes `logId`, `crewId`, `crewName`, `meetingId`, `meetingTitle`, `meetingDate`, `createdAt`, server-trimmed `excerpt`, nullable `coverPhotoUrl`, and `photoCount`. The list includes only logs authored by the current user, excludes soft-deleted logs (`deletedAt is null` only), excludes logs on deleted crews to stay aligned with the normal read path, and sorts by `createdAt desc`, then `logId desc`. `excerpt` reuses the crew feed rule of trimming the body to at most 120 characters, `coverPhotoUrl` is the first attached photo by `meeting_log_photos.id asc`, and `photoCount` is the total number of attached photos with `0` as the fallback when no photo exists.
`GET /api/users/me/created-meetings` is the login-required profile activity detail endpoint for the "created meetings" entry on the my-page hub. It returns `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each item includes `meetingId`, `title`, `status`, `date`, `time`, `crewId`, and `crewName`, which is enough for the frontend to render a list card and navigate into the existing meeting detail screen. The list includes only meetings where the current user is the host, keeps the same visible statuses as the profile-hub count (`RECRUITING`, `RECRUITMENT_CLOSED`, `COMPLETED`, `CANCELED`), excludes meetings that belong to deleted crews to stay aligned with the normal read path, and sorts by `meetingDate desc`, `meetingTime desc`, then `meetingId desc`.
`GET /api/users/me/joined-meetings` is the login-required profile activity detail endpoint for the "joined meetings" entry on the my-page hub. It returns `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each item includes `meetingId`, `title`, `themeName`, `crewId`, `crewName`, `date`, `time`, `status`, nullable `result`, and `canWriteReview`. The list includes only non-host meetings where the current user has participation history with status `JOINED`, `PENDING`, or `APPROVED`, excludes `LEFT`, and also excludes meetings that belong to deleted crews to stay aligned with the normal read path. Sorting is fixed to `meetingDate desc`, `meetingTime desc`, then `meetingId desc`. `canWriteReview` is `true` only when the meeting is `COMPLETED`, the current user has no active log for that meeting, and the user is not blocked by a soft-deleted log history (`DELETED_BLOCKED`).
`GET /api/users/me/crews` is the login-required profile activity detail endpoint for the "my crews" entry on the my-page hub. It returns `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each item includes `crewId`, `crewName`, `visibility`, `leaderNickname`, and nullable `coverImageUrl`. The list includes only crews where the current user has an `ACTIVE` membership and the crew itself is also `ACTIVE`. `PENDING`, `LEFT`, `REMOVED`, and deleted crews are excluded so the list reflects only the user's current approved crew memberships. Sorting is fixed to `crewName asc`, then `crewId asc`.
`GET /api/users/me/pending-crews` is the login-required profile activity detail endpoint for the "pending crews" entry on the my-page hub. It returns `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each item includes `joinRequestId`, `crewId`, `crewName`, `requestedAt`, and nullable `messageSummary`. The list includes only the current user's join requests with status `PENDING` where the target crew is still `PUBLIC` and `ACTIVE`, sorts by `requestedAt desc` then `joinRequestId desc`, and uses server-generated one-line message summaries. Blank messages are normalized to `null` so the frontend can render a "메시지 없음" fallback. `DELETE /api/users/me/pending-crews/{joinRequestId}` cancels the current user's own pending join request and returns `joinRequestId` plus `crewId`, which is enough for the frontend to remove that item from the current list immediately.
`GET /api/users/me/withdrawal-check` is the login-required pre-withdrawal validation endpoint for the account settings flow. It is read-only in this round and returns `canWithdraw`, `blockingActiveCrews`, and `blockingParticipatingMeetings`. `canWithdraw` is `true` only when both blocking lists are empty. `blockingActiveCrews` contains every current `ACTIVE` crew membership on `ACTIVE` crews, regardless of whether the user is a leader or a member. `blockingParticipatingMeetings` contains unfinished meetings on `ACTIVE` crews where the current user is either the host (`participationRole = HOST`) or an active participant with `JOINED` or `APPROVED` status (`participationRole = PARTICIPANT`). Unfinished means `RECRUITING` or `RECRUITMENT_CLOSED`; `PENDING`, `LEFT`, `COMPLETED`, and `CANCELED` do not block withdrawal. Meetings are ordered by `meetingDate asc`, `meetingTime asc`, then `meetingId asc` so the frontend can show what should be resolved first. This round does not execute withdrawal or cleanup yet; it only explains whether withdrawal is currently blocked and why.
`POST /api/users/me/withdrawal` is the login-required withdrawal execution endpoint. It accepts `reasonCode`, optional `reasonDetail`, and `confirmationChecked=true`. Supported reason codes are `NOT_USING`, `SERVICE_UNSATISFIED`, `LOW_ACTIVITY`, and `OTHER`. The server reruns the exact same blocking policy as `GET /api/users/me/withdrawal-check` immediately before execution, and returns `409 AUTH_WITHDRAWAL_NOT_ALLOWED` if the user gained a new active crew membership or unfinished meeting participation since the read step. On success, the backend stores a withdrawal history row, soft-deletes the current `users` and `auth_users` rows, returns `{withdrawnAt, canLogin:false}`, and also sends an expired auth cookie so the current session is logged out immediately. Soft delete here means the rows remain for policy and audit purposes, but profile fields are de-identified and withdrawn rows are hidden from normal read/auth paths. Record-style data linked by `userId` remains, and a later rejoin starts as a fresh login state rather than restoring the old account profile.
`GET /api/explore/themes`, `GET /api/explore/themes/{themeId}`, and `GET /api/explore/filters` are public read endpoints and do not require authentication. Explore search uses one keyword `q` across theme name, store name, region, and district; supports repeated `genres` params plus optional `region`, `district`, `page`, and `size`; and returns `{items, pageInfo}` with `hasNext` so the frontend can extend it to infinite scroll. Explore list cards now expose `isFavorited` as the current-user favorite boolean source of truth. When the request is unauthenticated, `isFavorited` is always `false`; when the request is authenticated, it is resolved from the current user's theme-favorite relation without an extra API call. Theme detail now includes `isFavorite` with the same rule, so cards and detail stay in sync for the same signed-in user. Theme detail still returns the core theme fields plus `relatedThemes`, where recommendations are limited to at most four other active themes from the same store, excluding the current theme itself. Missing `posterImageUrl`, `description`, and `externalLink` are returned as `null` so the frontend can handle fallback copy and imagery. `POST /api/themes/{themeId}/favorite` and `DELETE /api/themes/{themeId}/favorite` are login-required favorite mutation endpoints. Both are idempotent: repeated `POST` on an already-favorited theme succeeds as a no-op, and repeated `DELETE` on an already-unfavorited theme also succeeds as a no-op. Unfavorite uses hard delete on the `theme_favorites` relation table, and both responses return `{themeId, isFavorite, favoriteCount}` so the frontend can update card/detail state immediately.
`GET /api/explore/meeting-create/crews` is a login-required explore helper endpoint for the "create a meeting with this theme" flow. It always returns `{crews:[...]}` with `crewId` and `crewName` only, uses active crew memberships as the source of truth, filters out deleted crews, and returns `200 OK` with an empty array when the authenticated user has no active crews. Explore does not add a separate `meeting-defaults` API in this round; the frontend should reuse the existing public theme detail response as the default-value source for meeting creation.
`GET /api/archive/meetings` is a login-required personal archive read endpoint for completed meeting history. It returns only `COMPLETED` meetings related to the authenticated user, where "related" means the user was the host or has participation history with status `JOINED`, `LEFT`, `PENDING`, or `APPROVED`. The response shape is `{items, pageInfo}` with `page`, `size`, and `hasNext`, sorted by `meetingDate desc`, `meetingTime desc`, and `meetingId desc`. Archive cards include `meetingId`, `crewId`, `crewName`, `themeName`, `place`, `date`, `result`, and optional `posterImageUrl`. Poster lookup is resolved from the explore `themes` source of truth by matching `meeting.themeName`; when no active theme image is found, `posterImageUrl` is returned as `null`.
`POST /api/uploads/log-photos` is a login-required meeting-log helper endpoint for image upload. The `meeting` domain keeps the upload policy, but actual file persistence is delegated to a common storage layer. It accepts a single multipart `file`, validates `jpg`/`jpeg`/`png` only, enforces `<= 5MB`, stores the file under the shared local storage root at `storage/log-photos`, and returns `{url, sizeBytes}` so the frontend can reuse the existing `photos` array contract without change. Uploaded files are served back through `/uploads/log-photos/{storedName}` and this response shape is intentionally compatible with a future S3 swap.
`POST /api/meetings/{meetingId}/logs`, `PATCH /api/logs/{logId}`, `DELETE /api/crews/{crewId}/logs/{logId}`, and `GET /api/meetings/{meetingId}/logs/me` are login-required meetingLog endpoints for personal escape-log writing. Write access is allowed only for `COMPLETED` meetings and only when the authenticated user is the meeting host or has participation history with status `JOINED`, `PENDING`, or `APPROVED`. `LEFT` participation history is still visible in archive read paths, but it does not grant write permission for a new log. Each user can keep at most one log per meeting. The request body requires non-blank `body` (max 1000 chars) and optional `photos`, where each photo is stored as `{url, sizeBytes}` metadata. Those photo entries can now come directly from the upload helper endpoint. A user can attach up to five photos per meeting log, and each photo must declare `sizeBytes <= 5MB`. `GET /api/meetings/{meetingId}/logs/me` is a state-aware endpoint: it returns `EXISTS` with `logId` plus detail fields when an active log is readable, `NOT_WRITTEN` when the user has never written a log for that meeting, and `DELETED_BLOCKED` when a soft-deleted row exists and current policy still blocks rewriting. Delete is crew-scoped: the author can delete their own log without a reason, and the active crew leader can delete another member's log only with a non-blank `deleteReason`. Meeting logs are soft-deleted, so deleted rows remain stored with delete metadata, stay blocked for create, disappear from detail and crew feed reads, and surface through `logs/me` as `DELETED_BLOCKED` instead of a generic not-found.
`GET /api/crews/{crewId}/logs` and `GET /api/crews/{crewId}/logs/{logId}` are login-required crew page read endpoints for meeting-log consumption. Only active members of the target crew can read them; deleted crews return `404 CREW_NOT_FOUND`, and non-members or left/removed members receive `403 AUTH_ACCESS_DENIED`. The feed is sorted by `createdAt desc`, then `logId desc`, and returns `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each card item includes `logId`, `meetingId`, `authorNickname`, `meetingTitle`, `meetingDate`, `createdAt`, server-trimmed `excerpt`, first-photo `coverPhotoUrl` (or `null`), and `extraPhotoCount`. Detail stays inside the same crew route and returns the full body plus ordered photo URL list for the selected log.
`GET /api/crews/{crewId}/gallery` is a login-required crew page gallery endpoint for read-only meeting-photo cards. Only active members of the target crew can read it. Each card represents one `COMPLETED` meeting from that crew, but only when the meeting host has uploaded at least one active photo through existing meeting logs. The response shape is `{items, pageInfo}` with `page`, `size`, and `hasNext`. Each item includes `meetingId`, `meetingDate`, `meetingTitle`, the representative `coverPhotoUrl`, and `extraPhotoCount`. The representative photo is defined as the earliest uploaded photo among the host's active log photos for that meeting. `extraPhotoCount` is the total number of active photos attached to that meeting across all active logs minus that representative photo, with a floor of `0`. The list is sorted by `meetingDate desc`, then representative-photo `createdAt desc`, then `meetingId desc`, so the frontend can support a simple "load more" gallery while keeping list and modal-detail responsibilities separate.
`GET /api/crews/{crewId}/gallery/{meetingId}` is the login-required gallery detail endpoint for the round 07 modal and lightbox flow. It is still crew-scoped and available only to active members of that crew. The target meeting must be the same kind of gallery-eligible `COMPLETED` meeting exposed by the list endpoint (same crew, host has at least one active photo); otherwise the backend returns `404 GALLERY_NOT_FOUND`. The response includes `meetingId`, `meetingDate`, `meetingTitle`, `photos`, and `totalPhotoCount`. Each photo item contains a stable `photoId`, `url`, and `order`. Unlike the list representative-photo rule, the detail `photos` array contains **all active photos** attached to the meeting across all active logs, ordered by upload time (`createdAt asc`, then `photoId asc`) so the frontend can render a 3-column thumbnail grid, open a lightbox, and show a stable `1 / n` index without additional sorting logic.
`GET /api/crews/{crewId}` is the internal crew hub endpoint and is available only to joined crew members; it returns the crew summary, current user's role, `hasNotice`, and leader-only `pendingJoinRequestCount`.
`POST /api/crews/{crewId}/leave` is the joined-member-only crew leave endpoint; it removes the caller's membership when the caller is not the leader and does not host unfinished meetings in that crew. After success, internal crew access is revoked because membership checks continue to read `crew_members`.
`POST /api/crews/{crewId}/members/{targetUserId}/remove` is the current-leader-only forced-remove endpoint; it accepts only current `MEMBER` targets, cancels that target's unfinished hosted meetings in the same crew, marks joined participation in same-crew meetings as `LEFT`, removes the crew membership, and immediately revokes internal crew access.
`POST /api/crews/{crewId}/transfer-leadership` is the current-leader-only ownership transfer endpoint; it accepts a current member as `targetUserId`, swaps the current leader to `MEMBER` and the target member to `LEADER` in one transaction, and immediately changes what crew hub and members list read as the active leader.
`POST /api/crews/{crewId}/delete` is the current-leader-only crew delete endpoint; it requires exact crew-name re-entry, blocks deletion when any other `ACTIVE` member or unfinished meeting remains, marks the crew as `DELETED`, transitions the deleting leader membership to `LEFT`, and makes the crew disappear from public/internal consumers because normal crew reads now return only `ACTIVE` crews.
`PATCH /api/crews/{crewId}/visibility` is the leader-only crew visibility toggle endpoint; it updates only the current `visibility` (`PUBLIC` or `PRIVATE`) and immediately affects new explore exposure and direct join request availability while leaving existing pending join requests untouched.
`GET /api/crews/{crewId}/members` is the joined-member-only crew members list endpoint; it returns leader-first ordering and then remaining members by `joinedAt desc`, while currently unsupported profile fields use safe defaults (`profileImageUrl=null`, `bio=null`, `gender=null`, `escapeCount=0`).
`GET /api/crews/{crewId}/policies` is the joined-member-only crew policy read endpoint; it returns `{policyId,title,content}` records and responds with `200 OK` plus `[]` when no policy exists.
`POST /api/crews/{crewId}/meetings`, `PATCH /api/crews/{crewId}/meetings/{meetingId}`, `GET /api/crews/{crewId}/meetings`, and `GET /api/crews/{crewId}/meetings/{meetingId}` are joined-member-only meeting endpoints. Create and edit now share the same operating shape: `title`, `themeName`, `place`, `date`, `time`, `capacity`, `description`, optional `totalCost`, and optional `contactLink`. Create defaults new meetings to `status=RECRUITING` and `result=NOT_RECORDED`, detail returns `myParticipationStatus` as `NOT_JOINED` or `JOINED`, and edit is host-only for `RECRUITING` and `RECRUITMENT_CLOSED` meetings.
Meeting cost stays as guide information, not settlement data. The backend stores only the total amount in `totalCost`; if the frontend wants to show a per-person estimate, it should derive that from `totalCost / capacity` without changing persistence meaning. This round also keeps the current `themeName` string and free-text `place` model for compatibility because the theme/store source-of-truth structure does not exist in the backend yet. Capacity edits are treated as operating information, so shrinking below the current joined count is allowed and does not remove anyone automatically.
`POST /api/crews/{crewId}/meetings/{meetingId}/join` is the joined-member-only immediate join endpoint; a successful call persists or restores `JOINED`, blocks duplicate joins, and treats the meeting host as already joined from creation time. Legacy `PENDING` and `APPROVED` rows from the old request-based round are still read as `JOINED` for compatibility.
`DELETE /api/crews/{crewId}/meetings/{meetingId}/join` is the joined-member-only self-cancel endpoint; a joined member transitions back to `NOT_JOINED` by storing `LEFT`, while the host is explicitly blocked from canceling participation in their own meeting.
`POST /api/crews/{crewId}/meetings/{meetingId}/close-recruitment`, `/reopen-recruitment`, and `/complete` are host-only meeting state actions. `POST /cancel` is available to the meeting host or the crew leader. Allowed transitions are `RECRUITING -> RECRUITMENT_CLOSED`, `RECRUITMENT_CLOSED -> RECRUITING`, `RECRUITING|RECRUITMENT_CLOSED -> CANCELED`, and `RECRUITMENT_CLOSED -> COMPLETED`; disallowed transitions fail with `MEETING_INVALID_STATUS_TRANSITION`, and list/detail read the updated `meetings.status` immediately.
`POST /api/crews/{crewId}/meetings/{meetingId}/result` is the host-only meeting result record endpoint; it accepts only `SUCCESS` or `FAILURE`, and only when the meeting is already `COMPLETED` and the current `result` is `NOT_RECORDED`. Once recorded, the result cannot be changed in this round, and both list/detail read the updated `meetings.result` immediately.
Meeting automatic transitions do not add new public APIs in this round. Existing list/detail/join/manual-state-change/result-record paths apply `MeetingAutomaticTransitionService` first, so capacity reached or start-time passed meetings auto-close to `RECRUITMENT_CLOSED`, and meetings move to `COMPLETED` when start time + 6 hours has passed. `CANCELED` and already `COMPLETED` meetings are excluded from this auto-transition rule.
`GET /api/crews/{crewId}/join-requests/pending` is the leader main-page summary endpoint, and `GET /api/crews/{crewId}/join-requests` is the leader management endpoint with message and status included.
`GET /api/crews/{crewId}/invite-candidates` and `POST /api/crews/{crewId}/invites` are private-crew leader endpoints for direct invite flow; candidate list excludes existing members, already pending invite targets, and the leader themself.
`GET /api/crew-invites/me` returns the current full user's invite history, and `POST /api/crew-invites/{inviteId}/accept|reject` process only `PENDING` invites while keeping invite rows as status history.
Profile and nickname availability moved to `/api/users/...`; frontend consumers should stop calling legacy `/api/auth/profile` and `/api/auth/nickname-availability`.

## Explore source of truth

- Explore Round 01 introduces `stores` and `themes` as the backend source of truth for public escape-room discovery.
- `stores` owns branch-level information such as `name`, `region`, `district`, and `address`.
- `themes` owns card-level information such as `name`, `genre`, `posterImageUrl`, `difficulty`, `activityLabel`, `recommendedPlayers`, `runningTimeMinutes`, `favoriteCount`, and `isActive`.
- Search cards are theme-centered and join store information at read time.
- This round does not implement theme detail, favorites, or meeting auto-fill from explore.
- Explore Round 02A adds public theme detail on top of the existing list/filter APIs. Detail uses the same `stores` + `themes` source of truth and includes `relatedThemes` selected from the same store.
- Missing optional card fields are returned as `null`; the frontend should handle poster fallbacks and truncation.
- `themes.description` and `themes.external_link` are optional detail-only fields. They may be `null` when source data is missing.

## MeetingLog source of truth

- MeetingLog Round 02 introduces `meeting_logs` and `meeting_log_photos` as the backend source of truth for personal escape-log writing.
- `meeting_logs` owns the one-log-per-user-per-meeting record with `meetingId`, `authorUserId`, `body`, `createdAt`, and `updatedAt`.
- `meeting_log_photos` owns ordered photo URL rows for each log.
- MeetingLog upload follow-up adds a separate upload helper endpoint. The backend still stores only photo URLs plus `sizeBytes` metadata in the log domain, but it now delegates actual file persistence and URL generation to a shared common storage layer rooted at `bangpot.storage.root-directory`, while the `meeting` domain keeps only the log-photo-specific validation and endpoint contract.
- Missing photos are represented as an empty list, not `null`.

## Crew lifecycle status

- `crews.status` is the source of truth for whether a crew is still visible to normal consumers.
- `ACTIVE` crews are visible to existing public/internal crew consumers.
- `DELETED` crews remain in storage for history preservation, but normal repository reads treat them as not found.
- After crew deletion, public cards, join view, crew hub, members list, policies, and crew-scoped meeting reads should no longer expose that crew.

## User ownership split

- `auth_users` is now the source of truth only for authentication and completion state.
- `users` is now the source of truth for completed member profile data.
- `users.id` uses the same identifier as `auth_users.id` (shared primary key).
- A `users` row is created when temp completion succeeds and the user becomes `FULL`.
- `nickname` ownership moved to `users.nickname`; application code should no longer trust `auth_users.nickname`.
- Profile reads and writes under `/api/users/...` now use `users`.
- Crew and meeting domains should use `User` for member/profile data and use completion checks only through the shared access service, not by directly depending on `AuthUser`.

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
