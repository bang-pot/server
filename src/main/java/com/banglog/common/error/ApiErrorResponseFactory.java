package com.banglog.common.error;

import java.util.List;

import org.springframework.stereotype.Component;

import com.banglog.common.logging.RequestTrace;

@Component
public class ApiErrorResponseFactory {

	public ApiErrorResponse create(ApiErrorCode errorCode) {
		return create(errorCode, errorCode.message(), List.of());
	}

	public ApiErrorResponse create(ApiErrorCode errorCode, String message) {
		return create(errorCode, message, List.of());
	}

	public ApiErrorResponse create(ApiErrorCode errorCode, List<ApiErrorField> fieldErrors) {
		return create(errorCode, errorCode.message(), fieldErrors);
	}

	public ApiErrorResponse create(ApiErrorCode errorCode, String message, List<ApiErrorField> fieldErrors) {
		return new ApiErrorResponse(
			errorCode.code(),
			message,
			resolveRequestId(),
			fieldErrors == null ? List.of() : List.copyOf(fieldErrors)
		);
	}

	private String resolveRequestId() {
		String requestId = RequestTrace.currentRequestId();
		return requestId == null || requestId.isBlank() ? "na" : requestId;
	}
}
