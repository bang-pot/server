package com.bangpot.crew.domain.view;

import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;

public record CrewInviteCandidateAccessView(
	CrewVisibility visibility,
	CrewRole myRole
) {

	public static CrewInviteCandidateAccessView of(CrewVisibility visibility, CrewRole myRole) {
		return new CrewInviteCandidateAccessView(visibility, myRole);
	}
}
