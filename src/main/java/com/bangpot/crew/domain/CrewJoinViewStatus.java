package com.bangpot.crew.domain;

public enum CrewJoinViewStatus {
	/** 비로그인 사용자라 가입 신청 전에 로그인이 필요하다. */
	GUEST,
	/** 로그인했지만 가입 완료 전이라 프로필/약관 완료가 먼저 필요하다. */
	COMPLETION_REQUIRED,
	/** 공개 크루의 비멤버 완료 사용자라 가입 신청을 할 수 있다. */
	CAN_REQUEST,
	/** 현재 사용자의 가입 신청이 이미 접수되어 승인 대기 중이다. */
	PENDING,
	/** 현재 사용자가 이미 크루 멤버라 크루 페이지로 이동하면 된다. */
	MEMBER,
	/** 비공개 크루의 비멤버라 직접 가입 신청을 할 수 없다. */
	PRIVATE_RESTRICTED
}
