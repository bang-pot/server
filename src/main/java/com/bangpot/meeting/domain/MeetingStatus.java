package com.bangpot.meeting.domain;

public enum MeetingStatus {
	// Default right after creation; new participants can still join in later rounds.
	RECRUITING,
	// Recruitment is closed, but the meeting itself is not finished yet.
	RECRUITMENT_CLOSED,
	// The meeting has ended.
	COMPLETED,
	// The meeting was canceled before completion.
	CANCELED
}
