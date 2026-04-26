package com.bangpot.crew.domain.view;

import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;

public record CrewHubView(
	Long crewId,
	String name,
	String description,
	CrewVisibility visibility,
	String imageUrl,
	CrewRole myRole,
	boolean hasNotice,
	Integer pendingJoinRequestCount
) {

	public static CrewHubView of(
		Long crewId,
		String name,
		String description,
		CrewVisibility visibility,
		String imageUrl,
		CrewRole myRole,
		boolean hasNotice,
		Integer pendingJoinRequestCount
	) {
		return new CrewHubView(
			crewId,
			name,
			description,
			visibility,
			imageUrl,
			myRole,
			hasNotice,
			pendingJoinRequestCount
		);
	}

	public CrewHubView withPendingJoinRequestCount(Integer count) {
		return new CrewHubView(crewId, name, description, visibility, imageUrl, myRole, hasNotice, count);
	}
}
