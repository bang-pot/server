package com.banglog.crew.domain.view;

import com.banglog.crew.domain.CrewJoinViewStatus;
import com.banglog.crew.domain.CrewVisibility;

public record CrewJoinView(
	Long crewId,
	String name,
	String description,
	CrewVisibility visibility,
	String imageUrl,
	CrewJoinViewStatus myStatus
) {

	public static CrewJoinView of(
		Long crewId,
		String name,
		String description,
		CrewVisibility visibility,
		String imageUrl,
		CrewJoinViewStatus myStatus
	) {
		return new CrewJoinView(crewId, name, description, visibility, imageUrl, myStatus);
	}
}
