package com.bangpot.auth.application.exception;

public class InvalidNicknameException extends RuntimeException {

	public InvalidNicknameException() {
		super("닉네임은 비어 있을 수 없습니다.");
	}
}
