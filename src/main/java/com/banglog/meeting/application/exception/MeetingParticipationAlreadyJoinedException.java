package com.banglog.meeting.application.exception;

public class MeetingParticipationAlreadyJoinedException extends RuntimeException {

	public MeetingParticipationAlreadyJoinedException(Long meetingId, Long userId) {
		super("이미 참여 중인 모임입니다. meetingId=%d, userId=%d".formatted(meetingId, userId));
	}
}
