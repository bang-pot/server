package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetPublicCrewCardsUseCase {

	List<View> handle();

	record View(
		Long crewId,
		String name,
		String description,
		String visibility,
		String imageUrl
	) {
		public static View of(
			Long crewId,
			String name,
			String description,
			String visibility,
			String imageUrl
		) {
			return new View(crewId, name, description, visibility, imageUrl);
		}
	}
}
