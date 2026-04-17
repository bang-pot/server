package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyWithdrawalCheckUseCase {

	Result handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record Result(
		boolean canWithdraw,
		List<BlockingActiveCrew> blockingActiveCrews,
		List<BlockingParticipatingMeeting> blockingParticipatingMeetings
	) {
		public static Result of(
			boolean canWithdraw,
			List<BlockingActiveCrew> blockingActiveCrews,
			List<BlockingParticipatingMeeting> blockingParticipatingMeetings
		) {
			return new Result(canWithdraw, blockingActiveCrews, blockingParticipatingMeetings);
		}
	}

	record BlockingActiveCrew(
		Long crewId,
		String crewName
	) {
		public static BlockingActiveCrew of(Long crewId, String crewName) {
			return new BlockingActiveCrew(crewId, crewName);
		}
	}

	record BlockingParticipatingMeeting(
		Long meetingId,
		String meetingTitle,
		Long crewId,
		String crewName,
		String meetingStatus,
		String date,
		String time,
		String participationRole
	) {
		public static BlockingParticipatingMeeting of(
			Long meetingId,
			String meetingTitle,
			Long crewId,
			String crewName,
			String meetingStatus,
			String date,
			String time,
			String participationRole
		) {
			return new BlockingParticipatingMeeting(
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
