package com.bangpot.meeting.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum MeetingErrorCode implements ApiErrorCode {

	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
	MEETING_PARTICIPATION_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 참가 신청 대기 중입니다."),
	MEETING_PARTICIPATION_ALREADY_APPROVED(HttpStatus.CONFLICT, "이미 참가 확정된 모임입니다.");

	private final HttpStatus status;
	private final String message;

	MeetingErrorCode(HttpStatus status, String message) {
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
