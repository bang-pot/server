package com.bangpot.meeting.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum MeetingErrorCode implements ApiErrorCode {

	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
	MEETING_PARTICIPATION_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여 중인 모임입니다."),
	MEETING_PARTICIPATION_NOT_JOINED(HttpStatus.CONFLICT, "아직 참여하지 않은 모임입니다."),
	MEETING_HOST_CANNOT_CANCEL_PARTICIPATION(HttpStatus.CONFLICT, "모임장은 자신의 모임에서 참여취소할 수 없습니다."),
	MEETING_INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "허용되지 않은 모임 상태 전이입니다."),
	MEETING_RESULT_RECORD_NOT_ALLOWED(HttpStatus.CONFLICT, "모임 결과를 기록할 수 없는 상태입니다."),
	MEETING_RESULT_ALREADY_RECORDED(HttpStatus.CONFLICT, "이미 결과가 기록된 모임입니다.");

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
