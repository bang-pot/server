package com.banglog.crew.application.usecase;

import java.util.Locale;

import com.banglog.crew.application.exception.InvalidCrewVisibilityException;
import com.banglog.crew.domain.CrewVisibility;

public interface UpdateCrewVisibilityUseCase {

	Result handle(Command command);

	record Command(Long crewId, Long leaderUserId, CrewVisibility targetVisibility) {

		public static Command of(Long crewId, Long leaderUserId, String visibility) {
			return new Command(crewId, leaderUserId, parseVisibility(visibility));
		}

		private static CrewVisibility parseVisibility(String visibility) {
			if (visibility == null || visibility.isBlank()) {
				throw new InvalidCrewVisibilityException(visibility);
			}
			try {
				return CrewVisibility.valueOf(visibility.trim().toUpperCase(Locale.ROOT));
			}
			catch (IllegalArgumentException exception) {
				throw new InvalidCrewVisibilityException(visibility);
			}
		}
	}

	record Result(Long crewId, String visibility) {

		public static Result of(Long crewId, String visibility) {
			return new Result(crewId, visibility);
		}
	}
}
