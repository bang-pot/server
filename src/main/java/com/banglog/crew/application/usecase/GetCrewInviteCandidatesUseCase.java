package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.view.CrewInviteCandidatesView;

public interface GetCrewInviteCandidatesUseCase {

	CrewInviteCandidatesView handle(Query query);

	record Query(Long crewId, Long leaderUserId, String nickname, int page, int size) {
		public static Query of(Long crewId, Long leaderUserId, String nickname, int page, int size) {
			return new Query(crewId, leaderUserId, nickname, page, size);
		}
	}
}
