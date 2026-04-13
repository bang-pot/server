package com.bangpot.meeting.application.exception;

public class MeetingHostCannotCancelParticipationException extends RuntimeException {

	private final Long meetingId;
	private final Long userId;

	public MeetingHostCannotCancelParticipationException(Long meetingId, Long userId) {
		super("모임장은 자신의 모임에서 참여취소할 수 없습니다.");
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
