package com.bangpot.user.application.port;

import java.util.List;

public interface WithdrawalCheckReadRepository {

	View load(Long userId);

	record View(
		List<ActiveCrew> blockingActiveCrews,
		List<ParticipatingMeeting> blockingParticipatingMeetings
	) {
		public static View of(
			List<ActiveCrew> blockingActiveCrews,
			List<ParticipatingMeeting> blockingParticipatingMeetings
		) {
			return new View(blockingActiveCrews, blockingParticipatingMeetings);
		}
	}

	record ActiveCrew(
		Long crewId,
		String crewName
	) {
		public static ActiveCrew of(Long crewId, String crewName) {
			return new ActiveCrew(crewId, crewName);
		}
	}

	record ParticipatingMeeting(
		Long meetingId,
		String meetingTitle,
		Long crewId,
		String crewName,
		String meetingStatus,
		String date,
		String time,
		String participationRole
	) {
		public static ParticipatingMeeting of(
			Long meetingId,
			String meetingTitle,
			Long crewId,
			String crewName,
			String meetingStatus,
			String date,
			String time,
			String participationRole
		) {
			return new ParticipatingMeeting(
				meetingId,
				meetingTitle,
				crewId,
				crewName,
				meetingStatus,
				date,
				time,
				participationRole
			);
		}
	}
}
