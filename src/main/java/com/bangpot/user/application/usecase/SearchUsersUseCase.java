package com.bangpot.user.application.usecase;

import java.util.List;

public interface SearchUsersUseCase {

	Result handle(Query query);

	record Query(Long userId, String keyword, int size) {
		public static Query of(Long userId, String keyword, int size) {
			return new Query(userId, keyword, size);
		}
	}

	record Result(List<Item> items) {
		public static Result of(List<Item> items) {
			return new Result(items);
		}
	}

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
