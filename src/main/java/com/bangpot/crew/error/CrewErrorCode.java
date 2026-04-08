package com.bangpot.crew.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum CrewErrorCode implements ApiErrorCode {

	CREW_NOT_FOUND(HttpStatus.NOT_FOUND, "크루를 찾을 수 없습니다."),
	CREW_DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 크루명입니다."),
	CREW_JOIN_REQUEST_NOT_ALLOWED(HttpStatus.FORBIDDEN, "비공개 크루는 직접 가입 신청할 수 없습니다."),
	CREW_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입된 크루입니다."),
	CREW_JOIN_REQUEST_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 가입 신청 대기 중입니다.");

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
