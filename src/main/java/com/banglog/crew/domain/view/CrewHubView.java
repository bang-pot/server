package com.banglog.crew.domain.view;

import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewVisibility;

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

	public CrewHubView(
		Long crewId,
		String name,
		String description,
		CrewVisibility visibility,
		String imageUrl,
		CrewRole myRole,
		Boolean hasNotice,
		Long pendingJoinRequestCount
	) {
		this(
			crewId,
			name,
			description,
			visibility,
			imageUrl,
			myRole,
			Boolean.TRUE.equals(hasNotice),
			pendingJoinRequestCount == null ? null : Math.toIntExact(pendingJoinRequestCount)
		);
	}

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
