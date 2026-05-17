package com.banglog.crew.application.exception;

public class CrewLeaderLeaveNotAllowedException extends RuntimeException {

	public CrewLeaderLeaveNotAllowedException(Long crewId, Long userId) {
		super("크루장은 먼저 크루장을 위임한 뒤 탈퇴할 수 있습니다. crewId=" + crewId + ", userId=" + userId);
	}
}
