package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetMyCrewInvitesUseCase {

	List<View> handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record View(Long inviteId, Long crewId, String crewName, String inviterNickname, String status) {
		public static View of(Long inviteId, Long crewId, String crewName, String inviterNickname, String status) {
			return new View(inviteId, crewId, crewName, inviterNickname, status);
		}
	}
}
