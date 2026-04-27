package com.bangpot.crew.application.exception;

public class CrewInviteAlreadyPendingException extends RuntimeException {

	public CrewInviteAlreadyPendingException(Long crewId, Long targetUserId) {
		super("이미 대기 중인 크루 초대가 있습니다. crewId=" + crewId + ", targetUserId=" + targetUserId);
	}
}
