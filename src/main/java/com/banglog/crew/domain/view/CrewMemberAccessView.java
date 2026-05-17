package com.banglog.crew.domain.view;

import com.banglog.crew.domain.CrewRole;

public record CrewMemberAccessView(
	CrewRole myRole
) {

	public static CrewMemberAccessView of(CrewRole myRole) {
		return new CrewMemberAccessView(myRole);
	}
}
