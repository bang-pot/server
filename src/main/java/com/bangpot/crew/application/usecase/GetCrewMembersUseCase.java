package com.bangpot.crew.application.usecase;

import java.util.List;

import com.bangpot.crew.domain.CrewRole;

public interface GetCrewMembersUseCase {

	List<View> handle(Query query);

	record Query(Long crewId, Long userId) {

		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId);
		}
	}

	record View(
		Long userId,
		String nickname,
		String profileImageUrl,
		String bio,
		String gender,
		int escapeCount,
		CrewRole role,
		String joinedAt
	) {

		public static View of(
			Long userId,
			String nickname,
			String profileImageUrl,
			String bio,
			String gender,
			int escapeCount,
			CrewRole role,
			String joinedAt
		) {
			return new View(userId, nickname, profileImageUrl, bio, gender, escapeCount, role, joinedAt);
		}
	}
}
