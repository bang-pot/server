package com.banglog.auth.infrastructure;

public final class RedirectPathSanitizer {

	private RedirectPathSanitizer() {
	}

	public static String sanitize(String redirectPath) {
		if (redirectPath == null || redirectPath.isBlank()) {
			return null;
		}
		if (!redirectPath.startsWith("/") || redirectPath.startsWith("//")) {
			return null;
		}
		return redirectPath;
	}
}
