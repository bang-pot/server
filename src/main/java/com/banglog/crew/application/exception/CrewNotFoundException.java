package com.banglog.crew.application.exception;

public class CrewNotFoundException extends RuntimeException {

	public CrewNotFoundException(Long crewId) {
		super("크루를 찾을 수 없습니다. crewId=" + crewId);
	}
}
