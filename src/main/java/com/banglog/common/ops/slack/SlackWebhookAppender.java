package com.banglog.common.ops.slack;

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

import com.banglog.common.logging.RequestTrace;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;

public class SlackWebhookAppender extends AppenderBase<ILoggingEvent> {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		.withZone(ZoneId.systemDefault());
	private static final String STARTUP_LOGGER = "com.banglog.common.logging.StartupLifecycleLogger";
	private static final String STARTUP_FAILURE_LOGGER = "org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter";
	private static final Pattern MESSAGE_PATTERN = Pattern.compile("message=\"([^\"]+)\"");
	private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("requestId=([^\\s]+)");
	private static final Pattern PATH_PATTERN = Pattern.compile("path=([^\\s]+)");
	private static final Pattern CONTEXT_PATTERN = Pattern.compile("context=([^\\s]+)");
	private static final Pattern REASON_PATTERN = Pattern.compile("(?:exceptionType|failureType)=([^\\s]+)");
	private static final Pattern DURATION_PATTERN = Pattern.compile("durationMs=([^\\s]+)");
	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("Description:\\s*(.+?)(?:\\s+Action:|$)", Pattern.DOTALL);
	private static final String STARTUP_FAILURE_SUMMARY =
		"\uC560\uD50C\uB9AC\uCF00\uC774\uC158 \uC2DC\uC791 \uC2E4\uD328";
	private static final String DEFAULT_SUMMARY =
		"\uC6B4\uC601 \uC54C\uB9BC";

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

	boolean isSlackTarget(ILoggingEvent eventObject) {
		if (eventObject.getLevel().isGreaterOrEqual(Level.ERROR)) {
			return true;
		}
		if (eventObject.getLevel().isGreaterOrEqual(Level.WARN)) {
			return !isScannerNoise(eventObject.getFormattedMessage());
		}
		return Level.INFO.equals(eventObject.getLevel()) && STARTUP_LOGGER.equals(eventObject.getLoggerName());
	}

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
		String durationMs = extract(DURATION_PATTERN, formattedMessage);

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
		appendLine(text, "durationMs", durationMs);
		appendLine(text, "reason", reason);
		appendLine(text, "summary", summary);

		return "{\"text\":\"" + escapeJson(text.toString()) + "\"}";
	}

	private Map<String, String> safeMdcPropertyMap(ILoggingEvent event) {
		try {
			Map<String, String> mdc = event.getMDCPropertyMap();
			return mdc == null ? Collections.emptyMap() : mdc;
		} catch (NullPointerException ignored) {
			return Collections.emptyMap();
		}
	}

	private boolean isScannerNoise(String formattedMessage) {
		return formattedMessage != null
			&& formattedMessage.contains("event=auth.protected_resource_access_failed");
	}

	private String extract(Pattern pattern, String message) {
		if (message == null || message.isBlank()) {
			return null;
		}
		Matcher matcher = pattern.matcher(message);
		return matcher.find() ? normalizeWhitespace(matcher.group(1)) : null;
	}

	private String resolveSummary(ILoggingEvent event, String formattedMessage) {
		if (STARTUP_FAILURE_LOGGER.equals(event.getLoggerName())) {
			return STARTUP_FAILURE_SUMMARY;
		}
		return firstNonBlank(extract(MESSAGE_PATTERN, formattedMessage), formattedMessage, DEFAULT_SUMMARY);
	}

	private String resolveContext(ILoggingEvent event) {
		if (STARTUP_FAILURE_LOGGER.equals(event.getLoggerName())) {
			return "application.startup";
		}
		return null;
	}

	private String resolveReason(ILoggingEvent event, String formattedMessage) {
		if (STARTUP_FAILURE_LOGGER.equals(event.getLoggerName())) {
			return firstNonBlank(extract(DESCRIPTION_PATTERN, formattedMessage), "startup failed");
		}
		return extract(REASON_PATTERN, formattedMessage);
	}

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

	private String sanitizePath(String path) {
		String sanitized = sanitize(path);
		if (sanitized == null) {
			return null;
		}
		return RequestTrace.sanitizePath(sanitized);
	}

	private String firstNonBlank(String... candidates) {
		for (String candidate : candidates) {
			if (candidate != null && !candidate.isBlank()) {
				return candidate;
			}
		}
		return null;
	}

	private String normalizeWhitespace(String value) {
		return value == null ? null : value.replaceAll("\\s+", " ").trim();
	}

	private String escapeJson(String value) {
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\r", "")
			.replace("\n", "\\n");
	}
}
