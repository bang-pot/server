package com.bangpot.meeting.domain.view;

import com.bangpot.crew.domain.CrewRole;

public record MeetingsAccessView(
	Long crewId,
	CrewRole myRole
) {

	public static MeetingsAccessView of(Long crewId, CrewRole myRole) {
		return new MeetingsAccessView(crewId, myRole);
	}
}
