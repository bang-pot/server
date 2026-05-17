package com.banglog.auth.infrastructure.oauth;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

public class KakaoOAuth2User extends DefaultOAuth2User {

	private final long providerId;
	private final String nickname;

	public KakaoOAuth2User(
		Collection<? extends GrantedAuthority> authorities,
		Map<String, Object> attributes,
		String nameAttributeKey,
		long providerId,
		String nickname
	) {
		super(authorities, attributes, nameAttributeKey);
		this.providerId = providerId;
		this.nickname = nickname;
	}

	public long providerId() {
		return providerId;
	}

	public String nickname() {
		return nickname;
	}
}
