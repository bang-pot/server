package com.banglog.meeting.application.exception;

public class MeetingRecruitmentReopenNotAllowedException extends RuntimeException {

	public MeetingRecruitmentReopenNotAllowedException(Long meetingId) {
		super("모집 가능한 상태가 아니어서 모집을 재개할 수 없습니다. meetingId=" + meetingId);
	}
}
