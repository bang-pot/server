package com.banglog.crew.application.exception;

public class CrewInviteNotAllowedException extends RuntimeException {

	public CrewInviteNotAllowedException(Long crewId) {
		super("크루 직접 초대를 보낼 수 없습니다. crewId=" + crewId);
	}
}
