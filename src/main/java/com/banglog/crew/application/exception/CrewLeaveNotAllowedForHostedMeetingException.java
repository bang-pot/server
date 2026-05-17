package com.banglog.crew.application.exception;

public class CrewLeaveNotAllowedForHostedMeetingException extends RuntimeException {

	public CrewLeaveNotAllowedForHostedMeetingException(Long crewId, Long userId) {
		super("미완료 생성 모임이 있으면 크루를 탈퇴할 수 없습니다. crewId=" + crewId + ", userId=" + userId);
	}
}
