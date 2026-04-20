package com.bangpot.user.application.port;

import java.util.List;

public interface UserSearchReadRepository {

	List<Item> search(String keyword, int size);

	record Item(
		Long userId,
		String nickname,
		String profileImageUrl,
		String bio,
		String gender,
		int escapeCount
	) {
		public static Item of(
			Long userId,
			String nickname,
			String profileImageUrl,
			String bio,
			String gender,
			int escapeCount
		) {
			return new Item(userId, nickname, profileImageUrl, bio, gender, escapeCount);
		}
	}
}
