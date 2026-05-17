package com.banglog.meeting.application.usecase;

public interface CleanupMeetingsForInactiveCrewMemberUseCase {

	void handle(InactiveCrewMember inactiveCrewMember);

	record InactiveCrewMember(
		Long crewId,
		Long userId
	) {
		public static InactiveCrewMember of(Long crewId, Long userId) {
			return new InactiveCrewMember(crewId, userId);
		}
	}
}
