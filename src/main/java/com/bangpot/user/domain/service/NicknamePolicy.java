package com.bangpot.user.domain;

import java.util.regex.Pattern;

public final class NicknamePolicy {

	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣A-Za-z0-9]{2,12}$");

	private NicknamePolicy() {
	}

	public static String normalize(String nickname) {
		if (nickname == null) {
			return null;
		}
		String normalizedNickname = nickname.trim();
		if (normalizedNickname.isEmpty()) {
			return null;
		}
		if (!NICKNAME_PATTERN.matcher(normalizedNickname).matches()) {
			return null;
		}
		return normalizedNickname;
	}
}
