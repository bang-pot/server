package com.banglog.crew.application.exception;

public class CrewAlreadyJoinedException extends RuntimeException {

	public CrewAlreadyJoinedException(Long crewId, Long userId) {
		super("이미 가입된 크루입니다. crewId=" + crewId + ", userId=" + userId);
	}
}
