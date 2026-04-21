package com.bangpot.common.ops.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

class SlackWebhookAppenderTest {

	private static final String STARTUP_COMPLETED_MESSAGE = "\uC560\uD50C\uB9AC\uCF00\uC774\uC158 \uAE30\uB3D9 \uC644\uB8CC";
	private static final String PROTECTED_RESOURCE_MESSAGE = "\uBCF4\uD638 \uC790\uC6D0 \uC811\uADFC \uC2E4\uD328";
	private static final String SERVER_ERROR_MESSAGE = "\uC694\uCCAD \uCC98\uB9AC \uC911 \uC11C\uBC84 \uC624\uB958 \uBC1C\uC0DD";
	private static final String SLOW_REQUEST_MESSAGE = "\uB290\uB9B0 \uC694\uCCAD \uAC10\uC9C0";
	private static final String STARTUP_FAILURE_MESSAGE = "\uC560\uD50C\uB9AC\uCF00\uC774\uC158 \uC2DC\uC791 \uC2E4\uD328";

	@Test
	void targetsStartupInfoLog() {
		SlackWebhookAppender appender = appender();

		assertThat(appender.isSlackTarget(loggingEvent(
			Level.INFO,
			"com.bangpot.common.logging.StartupLifecycleLogger",
			"event=application.startup.completed message=\"" + STARTUP_COMPLETED_MESSAGE + "\""
		))).isTrue();
	}

	@Test
	void skipsScannerStyleProtectedResourceWarning() {
		SlackWebhookAppender appender = appender();

		assertThat(appender.isSlackTarget(loggingEvent(
			Level.WARN,
			"com.bangpot.auth.infrastructure.logging.AuthAuditLogger",
			"event=auth.protected_resource_access_failed message=\"" + PROTECTED_RESOURCE_MESSAGE + "\" path=/api/sonicos/tfa"
		))).isFalse();
	}

	@Test
	void buildsPayloadWithTrackingFieldsAndReason() {
		SlackWebhookAppender appender = appender();
		LoggingEvent event = loggingEvent(
			Level.ERROR,
			"com.bangpot.common.logging.ServerErrorLoggingFilter",
			"event=request.failed message=\"" + SERVER_ERROR_MESSAGE + "\" path=/api/auth/me?token=secret context=request.failed exceptionType=IllegalStateException"
		);
		event.setMDCPropertyMap(Map.of("requestId", "req-ops-500"));

		String payload = appender.buildPayload(event);

		assertThat(payload)
			.contains("\"text\":\"[ERROR] " + SERVER_ERROR_MESSAGE + "\\n")
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
			"event=request.slow message=\"" + SLOW_REQUEST_MESSAGE + "\" path=/api/auth/me context=request.slow durationMs=3200 thresholdMs=3000"
		);
		event.setMDCPropertyMap(Map.of("requestId", "req-slow-1"));

		String payload = appender.buildPayload(event);

		assertThat(payload)
			.contains("\"text\":\"[WARN] " + SLOW_REQUEST_MESSAGE + "\\n")
			.contains("- requestId: req-slow-1\\n")
			.contains("- path: /api/auth/me\\n")
			.contains("- context: request.slow\\n")
			.contains("- durationMs: 3200\\n")
			.contains("- summary: " + SLOW_REQUEST_MESSAGE);
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
			.contains("\"text\":\"[ERROR] " + STARTUP_FAILURE_MESSAGE + "\\n")
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
