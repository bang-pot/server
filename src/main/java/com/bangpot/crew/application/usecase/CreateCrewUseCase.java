package com.bangpot.crew.application.usecase;

import java.util.Locale;

import com.bangpot.crew.application.exception.InvalidCrewVisibilityException;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;

public interface CreateCrewUseCase {

	Result handle(Command command);

	record Command(
		Long userId,
		String name,
		String description,
		CrewVisibility visibility,
		String imageUrl
	) {
		public static Command of(
			Long userId,
			String name,
			String description,
			String visibility,
			String imageUrl
		) {
			return new Command(userId, name, description, parseVisibility(visibility), imageUrl);
		}

		private static CrewVisibility parseVisibility(String visibility) {
			if (visibility == null || visibility.isBlank()) {
				return null;
			}

			try {
				return CrewVisibility.valueOf(visibility.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException exception) {
				throw new InvalidCrewVisibilityException(visibility);
			}
		}
	}

	record Result(
		Long crewId,
		String name,
		CrewRole myRole
	) {
		public static Result of(Long crewId, String name, CrewRole myRole) {
			return new Result(crewId, name, myRole);
		}
	}
}
