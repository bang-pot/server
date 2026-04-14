package com.bangpot.meeting.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum MeetingLogErrorCode implements ApiErrorCode {

	LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "meeting log를 찾을 수 없습니다."),
	LOG_WRITE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태에서는 meeting log를 작성할 수 없습니다."),
	LOG_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 작성된 meeting log가 존재합니다.");

	private final HttpStatus status;
	private final String message;

	MeetingLogErrorCode(HttpStatus status, String message) {
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

