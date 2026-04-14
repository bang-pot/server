package com.bangpot.explore.application.exception;

public class ExploreThemeNotFoundException extends RuntimeException {

	public ExploreThemeNotFoundException(Long themeId) {
		super("Explore theme not found. themeId=" + themeId);
	}
}
