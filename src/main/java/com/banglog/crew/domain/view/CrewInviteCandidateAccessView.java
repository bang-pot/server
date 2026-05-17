package com.banglog.crew.domain.view;

import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewVisibility;

public record CrewInviteCandidateAccessView(
	CrewVisibility visibility,
	CrewRole myRole
) {

	public static CrewInviteCandidateAccessView of(CrewVisibility visibility, CrewRole myRole) {
		return new CrewInviteCandidateAccessView(visibility, myRole);
	}
}
