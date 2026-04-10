package com.bangpot.crew.application.exception;

public class CrewInviteAlreadyPendingException extends RuntimeException {

	public CrewInviteAlreadyPendingException(Long crewId, Long targetUserId) {
		super("이미 크루 초대가 대기 중입니다. crewId=" + crewId + ", targetUserId=" + targetUserId);
	}
}
