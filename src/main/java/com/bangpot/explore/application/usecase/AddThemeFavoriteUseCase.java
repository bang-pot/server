package com.bangpot.explore.application.usecase;

public interface AddThemeFavoriteUseCase {

	Result handle(Command command);

	record Command(Long userId, Long themeId) {
		public static Command of(Long userId, Long themeId) {
			return new Command(userId, themeId);
		}
	}

	record Result(Long themeId, boolean isFavorite, int favoriteCount) {
		public static Result of(Long themeId, boolean isFavorite, int favoriteCount) {
			return new Result(themeId, isFavorite, favoriteCount);
		}
	}
}
