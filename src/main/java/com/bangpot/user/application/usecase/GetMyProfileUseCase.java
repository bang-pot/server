package com.bangpot.user.application.usecase;

public interface GetMyProfileUseCase {

	View handle(Query query);

	record Query(Long userId) {

		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record View(
		Long id,
		String nickname,
		String profileImageUrl,
		Long createdMeetingsCount,
		Long joinedMeetingsCount,
		Long myCrewsCount,
		Long pendingCrewsCount
	) {

		public static View of(
			Long id,
			String nickname,
			String profileImageUrl,
			Long createdMeetingsCount,
			Long joinedMeetingsCount,
			Long myCrewsCount,
			Long pendingCrewsCount
		) {
			return new View(
				id,
				nickname,
				profileImageUrl,
				createdMeetingsCount,
				joinedMeetingsCount,
				myCrewsCount,
				pendingCrewsCount
			);
		}
	}
}
