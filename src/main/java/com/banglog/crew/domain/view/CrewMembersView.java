package com.banglog.crew.domain.view;

import java.time.Instant;
import java.util.List;

import com.banglog.crew.domain.CrewRole;

public record CrewMembersView(
	CrewRole myRole,
	List<CrewMembersView.Item> items,
	CrewMembersView.Page page
) {

	public static CrewMembersView of(CrewRole myRole, List<CrewMembersView.Item> items) {
		return new CrewMembersView(myRole, items, Page.of(0, items.size(), false));
	}

	public static CrewMembersView of(CrewRole myRole, List<CrewMembersView.Item> items, CrewMembersView.Page page) {
		return new CrewMembersView(myRole, items, page);
	}

	public record Page(
		int page,
		int size,
		boolean hasNext
	) {

		public static Page of(int page, int size, boolean hasNext) {
			return new Page(page, size, hasNext);
		}
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

		public Item(
			Long userId,
			String nickname,
			String profileImageUrl,
			String bio,
			String gender,
			Long escapeCount,
			CrewRole role,
			Instant joinedAt
		) {
			this(userId, nickname, profileImageUrl, bio, gender, Math.toIntExact(escapeCount), role, joinedAt);
		}
	}
}
