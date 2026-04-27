package com.bangpot.meeting.application.usecase;

public interface CleanupMeetingsForRemovedCrewMemberUseCase {

	void handle(RemovedCrewMember removedCrewMember);

	record RemovedCrewMember(
		Long crewId,
		Long userId
	) {
		public static RemovedCrewMember of(Long crewId, Long userId) {
			return new RemovedCrewMember(crewId, userId);
		}
	}
}
