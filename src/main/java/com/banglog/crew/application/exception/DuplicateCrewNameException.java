package com.banglog.crew.application.exception;

public class DuplicateCrewNameException extends RuntimeException {

	public DuplicateCrewNameException(String crewName) {
		super(crewName);
	}
}
