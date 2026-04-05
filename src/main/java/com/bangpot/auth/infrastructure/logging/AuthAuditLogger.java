package com.bangpot.auth.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.common.logging.RequestTrace;

@Component
public class AuthAuditLogger {

	private static final Logger log = LoggerFactory.getLogger(AuthAuditLogger.class);

	public void loginSucceeded(
		AuthProvider provider,
		Long userId,
		AuthUserStatus authStatus,
		boolean completionRequired
	) {
		log.info(
			"event=auth.login.success message=\"로그인 성공\" requestId={} provider={} userId={} authStatus={} completionRequired={}",
			RequestTrace.currentRequestId(),
			provider,
			userId,
			authStatus,
			completionRequired
		);
	}

	public void loginFailed(String provider, String path, String failureType) {
		log.warn(
			"event=auth.login.failure message=\"로그인 실패\" requestId={} provider={} path={} failureType={}",
			RequestTrace.currentRequestId(),
			provider,
			RequestTrace.sanitizePath(path),
			failureType
		);
	}

	public void logoutSucceeded(Long userId) {
		log.info(
			"event=auth.logout message=\"로그아웃 완료\" requestId={} userId={}",
			RequestTrace.currentRequestId(),
			userId == null ? "anonymous" : userId
		);
	}

	public void protectedResourceAccessFailed(String method, String path) {
		log.warn(
			"event=auth.protected_resource_access_failed message=\"보호 자원 접근 실패\" requestId={} method={} path={}",
			RequestTrace.currentRequestId(),
			method,
			RequestTrace.sanitizePath(path)
		);
	}

	public void accessDenied(Long userId, String method, String path) {
		log.warn(
			"event=auth.access_denied message=\"권한 거부\" requestId={} userId={} method={} path={}",
			RequestTrace.currentRequestId(),
			userId == null ? "unknown" : userId,
			method,
			RequestTrace.sanitizePath(path)
		);
	}

	public void authStateChanged(
		Long userId,
		AuthUserStatus previousStatus,
		AuthUserStatus currentStatus,
		String reason
	) {
		log.info(
			"event=auth.state.changed message=\"인증 상태 전환\" requestId={} userId={} previousStatus={} currentStatus={} reason={}",
			RequestTrace.currentRequestId(),
			userId,
			previousStatus,
			currentStatus,
			reason
		);
	}
}
