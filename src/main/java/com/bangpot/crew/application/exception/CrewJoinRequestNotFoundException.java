package com.bangpot.crew.application.exception;

public class CrewJoinRequestNotFoundException extends RuntimeException {

	public CrewJoinRequestNotFoundException(Long crewId, Long requestId) {
		super("가입 신청을 찾을 수 없습니다. crewId=" + crewId + ", requestId=" + requestId);
	}

	public CrewJoinRequestNotFoundException(Long requestId) {
		super("가입 신청을 찾을 수 없습니다. requestId=" + requestId);
	}
}
