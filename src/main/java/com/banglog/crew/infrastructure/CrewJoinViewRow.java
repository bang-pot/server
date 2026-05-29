package com.banglog.crew.infrastructure;

import com.banglog.crew.domain.CrewJoinViewStatus;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.crew.domain.view.CrewJoinView;

public record CrewJoinViewRow(
	Long crewId,
	String name,
	String description,
	CrewVisibility visibility,
	String imageUrl,
	String myStatus
) {

	CrewJoinView toView() {
		return CrewJoinView.of(
			crewId,
			name,
			description,
			visibility,
			imageUrl,
			CrewJoinViewStatus.valueOf(myStatus)
		);
	}
}
