package com.bangpot.user.application.exception;

public class DuplicateNicknameException extends RuntimeException {

	public DuplicateNicknameException(String nickname) {
		super("duplicate nickname: " + nickname);
	}
}
