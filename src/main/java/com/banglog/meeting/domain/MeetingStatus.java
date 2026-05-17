package com.banglog.meeting.domain;

public enum MeetingStatus {
	// 생성 직후 기본 상태이며, 이후 라운드에서 신규 참여를 받을 수 있는 상태다.
	RECRUITING,
	// 모집은 마감됐지만 모임 자체는 아직 종료되지 않은 상태다.
	RECRUITMENT_CLOSED,
	// 모임이 종료된 상태다.
	COMPLETED,
	// 모임이 종료 전에 취소된 상태다.
	CANCELED
}
