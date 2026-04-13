package com.bangpot.meeting.application.exception;

public class MeetingParticipationNotJoinedException extends RuntimeException {

	private final Long meetingId;
	private final Long userId;

	public MeetingParticipationNotJoinedException(Long meetingId, Long userId) {
		super("아직 참여하지 않은 모임입니다.");
		this.meetingId = meetingId;
		this.userId = userId;
	}

	public Long meetingId() {
		return meetingId;
	}

	public Long userId() {
		return userId;
	}
}
