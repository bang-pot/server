package com.banglog.common.error;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ApiErrorCode {

	COMMON_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	COMMON_IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더가 필요합니다."),
	COMMON_DUPLICATE_REQUEST(HttpStatus.CONFLICT, "이미 처리 중인 요청입니다."),
	COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

	private final HttpStatus status;
	private final String message;

	CommonErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public String message() {
		return message;
	}

	@Override
	public HttpStatus status() {
		return status;
	}
}
