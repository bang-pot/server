package com.bangpot.user.domain;

public class User {

	private final Long id;
	private String nickname;
	private final boolean profileCompleted;

	private User(Long id, String nickname, boolean profileCompleted) {
		this.id = id;
		this.nickname = nickname;
		this.profileCompleted = profileCompleted;
	}

	public static User rehydrate(Long id, String nickname, boolean profileCompleted) {
		return new User(id, nickname, profileCompleted);
	}

	public Long getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public boolean requiresCompletion() {
		return !profileCompleted;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}
}
