package com.banglog.crew.application.exception;

public class InvalidCrewVisibilityException extends RuntimeException {

	public InvalidCrewVisibilityException(String visibility) {
		super(visibility);
	}
}
