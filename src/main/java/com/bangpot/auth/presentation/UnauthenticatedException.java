package com.bangpot.auth.presentation;

class UnauthenticatedException extends RuntimeException {

	UnauthenticatedException() {
		super("인증이 필요합니다.");
	}
}
