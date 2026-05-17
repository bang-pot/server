package com.banglog.crew.application.exception;

public class CrewTransferLeadershipTargetNotAllowedException extends RuntimeException {

	public CrewTransferLeadershipTargetNotAllowedException(Long crewId, Long targetUserId) {
		super("위임 대상은 현재 일반 크루원만 가능합니다. crewId=" + crewId + ", targetUserId=" + targetUserId);
	}
}
