package com.banglog.user.domain.view;

import java.util.List;

public record UserSearchView(
	List<UserSearchView.Item> items,
	UserSearchView.Page page
) {
	public static UserSearchView of(List<UserSearchView.Item> items, UserSearchView.Page page) {
		return new UserSearchView(items, page);
	}

	public record Item(
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

	public record Page(
		int page,
		int size,
		long totalElements,
		int totalPages
	) {
		public static Page of(int page, int size, long totalElements, int totalPages) {
			return new Page(page, size, totalElements, totalPages);
		}
	}
}
