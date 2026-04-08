package com.bangpot.crew.domain;

public enum CrewJoinViewStatus {
	/** 비로그인 사용자라 가입 신청 전에 로그인 유도가 필요하다. */
	GUEST,
	/** 로그인했지만 TEMP 상태라 completion을 먼저 마쳐야 한다. */
	COMPLETION_REQUIRED,
	/** 공개 크루의 비소속 full 사용자라 바로 가입 신청할 수 있다. */
	CAN_REQUEST,
	/** 현재 사용자 기준 가입 신청이 이미 접수되어 승인 대기 중이다. */
	PENDING,
	/** 현재 사용자가 이미 이 크루의 멤버라 크루 페이지로 이동하면 된다. */
	MEMBER,
	/** 비공개 크루의 비소속 사용자라 직접 가입 신청할 수 없다. */
	PRIVATE_RESTRICTED
}
