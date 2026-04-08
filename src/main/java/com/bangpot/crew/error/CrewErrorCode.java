package com.bangpot.crew.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum CrewErrorCode implements ApiErrorCode {

	CREW_DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 크루명입니다.");

	private final HttpStatus status;
	private final String message;

	CrewErrorCode(HttpStatus status, String message) {
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
