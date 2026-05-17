package com.banglog.user.application.exception;

public class InvalidNicknameException extends RuntimeException {

	public InvalidNicknameException() {
		super("invalid nickname");
	}
}
