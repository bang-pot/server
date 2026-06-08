package com.banglog.crew.application.usecase;

public interface CheckCrewDeletionAvailabilityUseCase {

	Result handle(Query query);

	record Query(Long crewId, Long leaderUserId) {

		public static Query of(Long crewId, Long leaderUserId) {
			return new Query(crewId, leaderUserId);
		}
	}

	record Result(
		Long crewId,
		boolean canDelete,
		boolean hasOnlyLeader,
		boolean hasNoUnfinishedMeetings
	) {

		public static Result of(
			Long crewId,
			boolean canDelete,
			boolean hasOnlyLeader,
			boolean hasNoUnfinishedMeetings
		) {
			return new Result(crewId, canDelete, hasOnlyLeader, hasNoUnfinishedMeetings);
		}
	}
}
