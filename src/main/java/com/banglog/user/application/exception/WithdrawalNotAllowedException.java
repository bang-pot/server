package com.banglog.user.application.exception;

public class WithdrawalNotAllowedException extends RuntimeException {

	public WithdrawalNotAllowedException() {
		super("withdrawal is not allowed");
	}
}
