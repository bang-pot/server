package com.bangpot.user.domain;

public record UserSearchResult(
	Long userId,
	String nickname,
	String profileImageUrl,
	String bio,
	String gender,
	int escapeCount
) {
	public static UserSearchResult of(
		Long userId,
		String nickname,
		String profileImageUrl,
		String bio,
		String gender,
		int escapeCount
	) {
		return new UserSearchResult(userId, nickname, profileImageUrl, bio, gender, escapeCount);
	}
}
