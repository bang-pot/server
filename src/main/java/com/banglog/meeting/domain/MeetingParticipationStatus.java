package com.banglog.meeting.domain;

public enum MeetingParticipationStatus {
	NOT_JOINED,
	JOINED,
	LEFT,
	// 이전 request 기반 라운드의 레거시 값이며, 기존 row는 모두 JOINED로 해석한다.
	PENDING,
	APPROVED;

	public boolean representsJoined() {
		return this == JOINED || this == PENDING || this == APPROVED;
	}
}
