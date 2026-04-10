package com.bangpot.user.application.exception;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(Long userId) {
		super("user not found: " + userId);
	}
}
