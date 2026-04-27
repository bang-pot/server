package com.bangpot.crew.domain.view;

import com.bangpot.crew.domain.CrewRole;

public record CrewMemberAccessView(
	CrewRole myRole
) {

	public static CrewMemberAccessView of(CrewRole myRole) {
		return new CrewMemberAccessView(myRole);
	}
}
