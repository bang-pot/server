package com.banglog.crew.application.exception;

public class CrewSelfInviteNotAllowedException extends RuntimeException {

	public CrewSelfInviteNotAllowedException(Long crewId, Long userId) {
		super("자기 자신은 초대할 수 없습니다. crewId=" + crewId + ", userId=" + userId);
	}
}
