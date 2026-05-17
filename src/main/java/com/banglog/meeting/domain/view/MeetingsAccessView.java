package com.banglog.meeting.domain.view;

import com.banglog.crew.domain.CrewRole;

public record MeetingsAccessView(
	Long crewId,
	CrewRole myRole
) {

	public static MeetingsAccessView of(Long crewId, CrewRole myRole) {
		return new MeetingsAccessView(crewId, myRole);
	}
}
