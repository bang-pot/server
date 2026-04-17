package com.bangpot.user.application.port;

public interface ProfileHubReadRepository {

	Counts loadCounts(Long userId);

	record Counts(
		Long createdMeetingsCount,
		Long joinedMeetingsCount,
		Long myCrewsCount,
		Long pendingCrewsCount
	) {
		public static Counts of(
			Long createdMeetingsCount,
			Long joinedMeetingsCount,
			Long myCrewsCount,
			Long pendingCrewsCount
		) {
			return new Counts(createdMeetingsCount, joinedMeetingsCount, myCrewsCount, pendingCrewsCount);
		}
	}
}
