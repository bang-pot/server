package com.banglog.crew.domain.view;

import java.time.Instant;
import java.util.List;

import com.banglog.crew.domain.CrewRole;

public record CrewMembersView(
	CrewRole myRole,
	List<CrewMembersView.Item> items
) {

	public static CrewMembersView of(CrewRole myRole, List<CrewMembersView.Item> items) {
		return new CrewMembersView(myRole, items);
	}

	public record Item(
		Long userId,
		String nickname,
		String profileImageUrl,
		String bio,
		String gender,
		int escapeCount,
		CrewRole role,
		Instant joinedAt
	) {

		public static Item of(
			Long userId,
			String nickname,
			String profileImageUrl,
			String bio,
			String gender,
			int escapeCount,
			CrewRole role,
			Instant joinedAt
		) {
			return new Item(userId, nickname, profileImageUrl, bio, gender, escapeCount, role, joinedAt);
		}
	}
}
