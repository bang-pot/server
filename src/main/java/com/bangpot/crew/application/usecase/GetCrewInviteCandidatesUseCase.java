package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetCrewInviteCandidatesUseCase {

	List<View> handle(Query query);

	record Query(Long crewId, Long leaderUserId, String nickname) {
		public static Query of(Long crewId, Long leaderUserId, String nickname) {
			return new Query(crewId, leaderUserId, nickname);
		}
	}

	record View(Long userId, String nickname) {
		public static View of(Long userId, String nickname) {
			return new View(userId, nickname);
		}
	}
}
