package com.banglog.auth.infrastructure.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.domain.AuthUserStatus;

@ExtendWith(OutputCaptureExtension.class)
class AuthAuditLoggerTest {

	private final AuthAuditLogger authAuditLogger = new AuthAuditLogger();

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void writesTraceableLoginSuccessWithoutLeakingSecrets(CapturedOutput output) {
		MDC.put("requestId", "req-login-1");

		authAuditLogger.loginSucceeded(AuthProvider.KAKAO, 7L, AuthUserStatus.TEMP, true);

		assertThat(output.getOut())
			.contains("event=auth.login.success")
			.contains("message=\"로그인 성공\"")
			.contains("requestId=req-login-1")
			.contains("provider=KAKAO")
			.contains("userId=7")
			.contains("authStatus=TEMP")
			.contains("completionRequired=true")
			.doesNotContain("token=")
			.doesNotContain("cookie=")
			.doesNotContain("secret=");
	}

	@Test
	void writesSecurityFailureEventsWithRequestTrace(CapturedOutput output) {
		MDC.put("requestId", "req-authz-1");

		authAuditLogger.protectedResourceAccessFailed("GET", "/api/crew");
		authAuditLogger.accessDenied(99L, "POST", "/api/admin");
		authAuditLogger.logoutSucceeded(99L);

		assertThat(output.getOut())
			.contains("event=auth.protected_resource_access_failed")
			.contains("code=AUTH_UNAUTHENTICATED")
			.contains("message=\"보호 자원 접근 실패\"")
			.contains("event=auth.access_denied")
			.contains("code=AUTH_ACCESS_DENIED")
			.contains("message=\"권한 거부\"")
			.contains("event=auth.logout")
			.contains("message=\"로그아웃 완료\"")
			.contains("requestId=req-authz-1")
			.contains("path=/api/crew")
			.contains("path=/api/admin");
	}
}
