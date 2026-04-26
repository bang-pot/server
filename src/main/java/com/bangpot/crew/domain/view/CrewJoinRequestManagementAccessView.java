package com.bangpot.crew.domain.view;

import com.bangpot.crew.domain.CrewRole;

public record CrewJoinRequestManagementAccessView(
	CrewRole myRole
) {

	public static CrewJoinRequestManagementAccessView of(CrewRole myRole) {
		return new CrewJoinRequestManagementAccessView(myRole);
	}
}
