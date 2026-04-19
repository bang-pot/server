package com.bangpot.auth.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum AuthErrorCode implements ApiErrorCode {

	AUTH_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "?몄쬆???꾩슂?⑸땲??"),
	AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "?묎렐 沅뚰븳???놁뒿?덈떎."),
	AUTH_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "?됰꽕?꾩씠 ?щ컮瑜댁? ?딆뒿?덈떎."),
	AUTH_REQUIRED_TERMS_AGREEMENT(HttpStatus.BAD_REQUEST, "?꾩닔 ?쎄? ?숈쓽媛 ?꾩슂?⑸땲??"),
	AUTH_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "?대? ?ъ슜 以묒씤 ?됰꽕?꾩엯?덈떎."),
	AUTH_USER_NOT_FOUND(HttpStatus.CONFLICT, "?몄쬆 ?ъ슜?먮? 李얠쓣 ???놁뒿?덈떎."),
	AUTH_COMPLETION_NOT_ALLOWED(HttpStatus.CONFLICT, "?대? 媛???꾨즺???ъ슜?먯엯?덈떎."),
	AUTH_WITHDRAWAL_NOT_ALLOWED(HttpStatus.CONFLICT, "withdrawal is not allowed right now");

	private final HttpStatus status;
	private final String message;

	AuthErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public String message() {
		return message;
	}

	@Override
	public HttpStatus status() {
		return status;
	}
}
