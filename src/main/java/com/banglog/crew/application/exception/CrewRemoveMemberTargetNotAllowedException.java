package com.banglog.crew.application.exception;

public class CrewRemoveMemberTargetNotAllowedException extends RuntimeException {

	public CrewRemoveMemberTargetNotAllowedException(Long crewId, Long targetUserId) {
		super("강제 제거 대상은 현재 일반 크루원만 가능합니다. crewId=" + crewId + ", targetUserId=" + targetUserId);
	}
}
