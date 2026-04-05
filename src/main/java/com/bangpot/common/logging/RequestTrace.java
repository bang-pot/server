package com.bangpot.common.logging;

import java.util.UUID;

import org.slf4j.MDC;

public final class RequestTrace {

	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	public static final String REQUEST_ID_MDC_KEY = "requestId";

	private RequestTrace() {
	}

	public static String currentRequestId() {
		String requestId = MDC.get(REQUEST_ID_MDC_KEY);
		return requestId == null || requestId.isBlank() ? "n/a" : requestId;
	}

	public static String resolveRequestId(String headerValue) {
		if (headerValue != null) {
			String normalized = headerValue.trim();
			if (!normalized.isEmpty() && normalized.length() <= 128) {
				return normalized;
			}
		}
		return UUID.randomUUID().toString();
	}

	public static String sanitizePath(String path) {
		if (path == null || path.isBlank()) {
			return "/";
		}
		int queryStart = path.indexOf('?');
		return queryStart >= 0 ? path.substring(0, queryStart) : path;
	}
}
