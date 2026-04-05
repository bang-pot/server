package com.bangpot.common.ops.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

class SlackWebhookAppenderTest {

	@Test
	void targetsStartupInfoLog() {
		SlackWebhookAppender appender = appender();

		assertThat(appender.isSlackTarget(loggingEvent(
			Level.INFO,
			"com.bangpot.common.logging.StartupLifecycleLogger",
			"event=application.startup.completed message=\"애플리케이션 기동 완료\""
		))).isTrue();
	}

	@Test
	void skipsScannerStyleProtectedResourceWarning() {
		SlackWebhookAppender appender = appender();

		assertThat(appender.isSlackTarget(loggingEvent(
			Level.WARN,
			"com.bangpot.auth.infrastructure.logging.AuthAuditLogger",
			"event=auth.protected_resource_access_failed message=\"보호 자원 접근 실패\" path=/api/sonicos/tfa"
		))).isFalse();
	}

	@Test
	void buildsPayloadWithTrackingFieldsAndReason() {
		SlackWebhookAppender appender = appender();
		LoggingEvent event = loggingEvent(
			Level.ERROR,
			"com.bangpot.common.logging.ServerErrorLoggingFilter",
			"event=request.failed message=\"요청 처리 중 서버 오류 발생\" path=/api/auth/me?token=secret context=request.failed exceptionType=IllegalStateException"
		);
		event.setMDCPropertyMap(Map.of("requestId", "req-ops-500"));

		String payload = appender.buildPayload(event);

		assertThat(payload)
			.contains("\"text\":\"[ERROR] 요청 처리 중 서버 오류 발생\\n")
			.contains("- service: bangpot-backend\\n")
			.contains("- environment: prod\\n")
			.contains("- requestId: req-ops-500\\n")
			.contains("- path: /api/auth/me\\n")
			.contains("- context: request.failed\\n")
			.contains("- reason: IllegalStateException\\n")
			.doesNotContain("token=secret");
	}

	@Test
	void buildsSlowRequestPayloadWithDuration() {
		SlackWebhookAppender appender = appender();
		LoggingEvent event = loggingEvent(
			Level.WARN,
			"com.bangpot.common.logging.RequestTracingFilter",
			"event=request.slow message=\"느린 요청 감지\" path=/api/auth/me context=request.slow durationMs=3200 thresholdMs=3000"
		);
		event.setMDCPropertyMap(Map.of("requestId", "req-slow-1"));

		String payload = appender.buildPayload(event);

		assertThat(payload)
			.contains("\"text\":\"[WARN] 느린 요청 감지\\n")
			.contains("- requestId: req-slow-1\\n")
			.contains("- path: /api/auth/me\\n")
			.contains("- context: request.slow\\n")
			.contains("- durationMs: 3200\\n")
			.contains("- summary: 느린 요청 감지");
	}

	@Test
	void buildsStartupFailurePayloadWithShortSummaryAndDescriptionReason() {
		SlackWebhookAppender appender = appender();
		LoggingEvent event = loggingEvent(
			Level.ERROR,
			"org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter",
			"""
			***************************
			APPLICATION FAILED TO START
			***************************

			Description:

			Web server failed to start. Port 8080 was already in use.

			Action:

			Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
			"""
		);

		String payload = appender.buildPayload(event);

		assertThat(payload)
			.contains("\"text\":\"[ERROR] 애플리케이션 시작 실패\\n")
			.contains("- context: application.startup\\n")
			.contains("- reason: Web server failed to start. Port 8080 was already in use.\\n")
			.doesNotContain("APPLICATION FAILED TO START")
			.doesNotContain("Action:");
	}

	private SlackWebhookAppender appender() {
		SlackWebhookAppender appender = new SlackWebhookAppender();
		appender.setService("bangpot-backend");
		appender.setEnvironment("prod");
		appender.setUrl("https://hooks.slack.test/services/ops");
		return appender;
	}

	private LoggingEvent loggingEvent(Level level, String loggerName, String message) {
		LoggingEvent event = new LoggingEvent();
		event.setLoggerContext(new LoggerContext());
		event.setLevel(level);
		event.setLoggerName(loggerName);
		event.setMessage(message);
		event.setTimeStamp(1_777_777_777_000L);
		return event;
	}
}
