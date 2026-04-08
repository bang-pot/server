package com.bangpot.crew.application.exception;

public class CrewJoinRequestAlreadyPendingException extends RuntimeException {

	public CrewJoinRequestAlreadyPendingException(Long crewId, Long userId) {
		super("이미 가입 신청 대기 중입니다. crewId=" + crewId + ", userId=" + userId);
	}
}
