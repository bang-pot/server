package com.banglog.user.domain.view;

public record UserProfileView(
	Long id,
	String nickname,
	String profileImageUrl
) {
	public static UserProfileView of(Long id, String nickname, String profileImageUrl) {
		return new UserProfileView(id, nickname, profileImageUrl);
	}
}
