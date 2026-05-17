package com.banglog.user.domain;

import lombok.Getter;

@Getter
public class User {

	private final Long id;
	private String nickname;
	private String profileImageUrl;

	private User(Long id, String nickname, String profileImageUrl) {
		this.id = id;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}

	public static User create(Long id, String nickname) {
		return new User(id, nickname, null);
	}

	public static User rehydrate(Long id, String nickname, String profileImageUrl) {
		return new User(id, nickname, profileImageUrl);
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateProfile(String nickname, String profileImageUrl) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}
}
