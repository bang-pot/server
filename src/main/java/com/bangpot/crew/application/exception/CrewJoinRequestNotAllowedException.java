package com.bangpot.crew.application.exception;

public class CrewJoinRequestNotAllowedException extends RuntimeException {

	public CrewJoinRequestNotAllowedException(Long crewId) {
		super("비공개 크루는 직접 가입 신청할 수 없습니다. crewId=" + crewId);
	}
}
