package com.bangpot.crew.application.exception;

public class CrewInviteNotFoundException extends RuntimeException {

	private final Long inviteId;

	public CrewInviteNotFoundException(Long inviteId) {
		super("초대를 찾을 수 없습니다. inviteId=" + inviteId);
		this.inviteId = inviteId;
	}

	public Long getInviteId() {
		return inviteId;
	}
}
