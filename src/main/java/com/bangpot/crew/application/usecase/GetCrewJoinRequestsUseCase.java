package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetCrewJoinRequestsUseCase {

	List<View> handle(Query query);

	record Query(Long crewId, Long leaderUserId) {
		public static Query of(Long crewId, Long leaderUserId) {
			return new Query(crewId, leaderUserId);
		}
	}

	record View(Long requestId, Long userId, String nickname, String message, String status) {
		public static View of(Long requestId, Long userId, String nickname, String message, String status) {
			return new View(requestId, userId, nickname, message, status);
		}
	}
}
