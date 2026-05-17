package com.banglog.auth.application.exception;

public class MissingRequiredTermsAgreementException extends RuntimeException {

	public MissingRequiredTermsAgreementException() {
		super("필수 약관 동의가 필요합니다.");
	}
}
