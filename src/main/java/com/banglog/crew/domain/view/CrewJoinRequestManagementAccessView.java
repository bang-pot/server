package com.banglog.crew.domain.view;

import com.banglog.crew.domain.CrewRole;

public record CrewJoinRequestManagementAccessView(
	CrewRole myRole
) {

	public static CrewJoinRequestManagementAccessView of(CrewRole myRole) {
		return new CrewJoinRequestManagementAccessView(myRole);
	}
}
