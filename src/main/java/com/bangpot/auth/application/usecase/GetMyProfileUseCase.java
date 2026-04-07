package com.bangpot.auth.application.usecase;

public interface GetMyProfileUseCase {

	View handle(Query query);

	record Query(Long userId) {

		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record View(Long id, String nickname) {

		public static View of(Long id, String nickname) {
			return new View(id, nickname);
		}
	}
}
