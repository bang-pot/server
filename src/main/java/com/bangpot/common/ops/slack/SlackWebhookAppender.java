package com.bangpot.common.ops.slack;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bangpot.common.logging.RequestTrace;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;

public class SlackWebhookAppender extends AppenderBase<ILoggingEvent> {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		.withZone(ZoneId.systemDefault());
	private static final String STARTUP_LOGGER = "com.bangpot.common.logging.StartupLifecycleLogger";
	private static final String STARTUP_FAILURE_LOGGER = "org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter";
	private static final Pattern MESSAGE_PATTERN = Pattern.compile("message=\"([^\"]+)\"");
	private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("requestId=([^\\s]+)");
	private static final Pattern PATH_PATTERN = Pattern.compile("path=([^\\s]+)");
	private static final Pattern CONTEXT_PATTERN = Pattern.compile("context=([^\\s]+)");
	private static final Pattern REASON_PATTERN = Pattern.compile("(?:exceptionType|failureType)=([^\\s]+)");
	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("Description:\\s*(.+?)(?:\\s+Action:|$)", Pattern.DOTALL);

	private final HttpClient httpClient = HttpClient.newBuilder()
		.connectTimeout(CONNECT_TIMEOUT)
		.build();

	@Setter
	private String url;

	@Setter
	private String service;

	@Setter
	private String environment;

	@Override
	// 현재 로그가 운영 알림 정책에 맞을 때만 Slack webhook으로 전송한다.
	protected void append(ILoggingEvent eventObject) {
		if (url == null || url.isBlank() || !isSlackTarget(eventObject)) {
			return;
		}

		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(REQUEST_TIMEOUT)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(buildPayload(eventObject)))
				.build();

			httpClient.send(request, HttpResponse.BodyHandlers.discarding());
		} catch (Exception exception) {
			addError("Failed to send Slack webhook log", exception);
		}
	}

	// startup 완료 로그, 운영상 중요한 warn, 모든 error만 Slack 대상으로 제한한다.
	boolean isSlackTarget(ILoggingEvent eventObject) {
		if (eventObject.getLevel().isGreaterOrEqual(Level.ERROR)) {
			return true;
		}
		if (eventObject.getLevel().isGreaterOrEqual(Level.WARN)) {
			return !isScannerNoise(eventObject.getFormattedMessage());
		}
		return Level.INFO.equals(eventObject.getLevel()) && STARTUP_LOGGER.equals(eventObject.getLoggerName());
	}

	// 로그 이벤트에서 최소 추적 필드를 뽑아 Slack text payload JSON으로 만든다.
	String buildPayload(ILoggingEvent event) {
		Map<String, String> mdc = safeMdcPropertyMap(event);
		String formattedMessage = sanitize(event.getFormattedMessage());
		String summary = resolveSummary(event, formattedMessage);
		String requestId = firstNonBlank(
			mdc.get(RequestTrace.REQUEST_ID_MDC_KEY),
			extract(REQUEST_ID_PATTERN, formattedMessage),
			"na"
		);
		String path = sanitizePath(extract(PATH_PATTERN, formattedMessage));
		String context = firstNonBlank(sanitize(extract(CONTEXT_PATTERN, formattedMessage)), resolveContext(event));
		String reason = resolveReason(event, formattedMessage);

		StringBuilder text = new StringBuilder();
		text.append('[')
			.append(event.getLevel())
			.append("] ")
			.append(summary);
		appendLine(text, "timestamp", TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp())));
		appendLine(text, "service", firstNonBlank(service, "unknown"));
		appendLine(text, "environment", firstNonBlank(environment, "unknown"));
		appendLine(text, "requestId", requestId);
		appendLine(text, "path", path);
		appendLine(text, "context", context);
		appendLine(text, "reason", reason);
		appendLine(text, "summary", summary);

		return "{\"text\":\"" + escapeJson(text.toString()) + "\"}";
	}

	// startup 실패처럼 MDC 자체가 아직 준비되지 않은 로그도 안전하게 처리한다.
	private Map<String, String> safeMdcPropertyMap(ILoggingEvent event) {
		try {
			Map<String, String> mdc = event.getMDCPropertyMap();
			return mdc == null ? Collections.emptyMap() : mdc;
		} catch (NullPointerException ignored) {
			return Collections.emptyMap();
		}
	}

	// 스캐너성 401 요청은 로그에는 남기고 Slack에서는 제외한다.
	private boolean isScannerNoise(String formattedMessage) {
		return formattedMessage != null
			&& formattedMessage.contains("event=auth.protected_resource_access_failed");
	}

	// 정해진 패턴이 보이면 로그 메시지에서 구조화된 필드 하나를 추출한다.
	private String extract(Pattern pattern, String message) {
		if (message == null || message.isBlank()) {
			return null;
		}
		Matcher matcher = pattern.matcher(message);
		return matcher.find() ? normalizeWhitespace(matcher.group(1)) : null;
	}

	// startup 실패는 긴 원문 대신 짧은 제목으로 고정하고, 나머지는 기존 message 필드를 우선 사용한다.
	private String resolveSummary(ILoggingEvent event, String formattedMessage) {
		if (STARTUP_FAILURE_LOGGER.equals(event.getLoggerName())) {
			return "애플리케이션 시작 실패";
		}
		return firstNonBlank(extract(MESSAGE_PATTERN, formattedMessage), formattedMessage, "운영 알림");
	}

	// startup 실패는 공통 context를 붙여 Slack 채널에서 종류를 바로 구분할 수 있게 한다.
	private String resolveContext(ILoggingEvent event) {
		if (STARTUP_FAILURE_LOGGER.equals(event.getLoggerName())) {
			return "application.startup";
		}
		return null;
	}

	// 일반 오류는 exception/failure type을, startup 실패는 Description 요약을 reason으로 사용한다.
	private String resolveReason(ILoggingEvent event, String formattedMessage) {
		if (STARTUP_FAILURE_LOGGER.equals(event.getLoggerName())) {
			return firstNonBlank(extract(DESCRIPTION_PATTERN, formattedMessage), "startup failed");
		}
		return extract(REASON_PATTERN, formattedMessage);
	}

	// 마스킹 이후에도 의미가 남는 값만 Slack 본문 한 줄로 추가한다.
	private void appendLine(StringBuilder text, String key, String value) {
		String sanitizedValue = sanitize(value);
		if (sanitizedValue == null || sanitizedValue.isBlank()) {
			return;
		}
		text.append("\n- ")
			.append(key)
			.append(": ")
			.append(sanitizedValue);
	}

	// Slack payload에 넣기 전에 민감정보 패턴을 먼저 마스킹한다.
	private String sanitize(String value) {
		if (value == null || value.isBlank() || "-".equals(value)) {
			return null;
		}
		String sanitized = value;
		sanitized = sanitized.replaceAll("Bearer\\s+[A-Za-z0-9._\\-+/=]+", "Bearer [REDACTED]");
		sanitized = sanitized.replaceAll("(?i)authorization[:=]\\s*[^\\s]+", "authorization=[REDACTED]");
		sanitized = sanitized.replaceAll(
			"(?i)(password|refresh[_-]?token|access[_-]?token|cookie|secret)[:=]\\s*[^\\s]+",
			"$1=[REDACTED]"
		);
		return normalizeWhitespace(sanitized);
	}

	// 일반 마스킹 규칙을 적용한 뒤 query string을 제거해 path-only 값으로 맞춘다.
	private String sanitizePath(String path) {
		String sanitized = sanitize(path);
		if (sanitized == null) {
			return null;
		}
		return RequestTrace.sanitizePath(sanitized);
	}

	// 후보 값 중 비어 있지 않은 첫 번째 값을 골라 fallback 순서를 고정한다.
	private String firstNonBlank(String... candidates) {
		for (String candidate : candidates) {
			if (candidate != null && !candidate.isBlank()) {
				return candidate;
			}
		}
		return null;
	}

	// 여러 줄과 중복 공백을 한 줄 요약 형태로 정리한다.
	private String normalizeWhitespace(String value) {
		return value == null ? null : value.replaceAll("\\s+", " ").trim();
	}

	// Slack webhook 요청에 넣을 수 있도록 text를 한 필드짜리 JSON 문자열로 이스케이프한다.
	private String escapeJson(String value) {
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\r", "")
			.replace("\n", "\\n");
	}
}
