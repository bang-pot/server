package com.bangpot.user.domain;

import lombok.Getter;

@Getter
public class User {

	private final Long id;
	private String nickname;

	private User(Long id, String nickname) {
		this.id = id;
		this.nickname = nickname;
	}

	public static User create(Long id, String nickname) {
		return new User(id, nickname);
	}

	public static User rehydrate(Long id, String nickname) {
		return new User(id, nickname);
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}
}
