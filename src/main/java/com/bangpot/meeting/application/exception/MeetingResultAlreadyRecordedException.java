package com.bangpot.meeting.application.exception;

public class MeetingResultAlreadyRecordedException extends RuntimeException {

	public MeetingResultAlreadyRecordedException(Long meetingId) {
		super("이미 결과가 기록된 모임입니다. meetingId=%d".formatted(meetingId));
	}

	public MeetingResultAlreadyRecordedException(Long meetingId, String currentResult) {
		super("이미 결과가 기록된 모임입니다. meetingId=%d, currentResult=%s".formatted(meetingId, currentResult));
	}
}
