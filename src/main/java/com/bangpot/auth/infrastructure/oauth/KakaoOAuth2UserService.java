package com.bangpot.auth.infrastructure.oauth;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class KakaoOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User user = delegate.loadUser(userRequest);
		Map<String, Object> attributes = user.getAttributes();

		Object idValue = attributes.get(userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName());
		if (!(idValue instanceof Number idNumber)) {
			throw new OAuth2AuthenticationException("Missing Kakao provider id.");
		}

		Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");
		Map<String, Object> profile = kakaoAccount == null ? null : getMap(kakaoAccount, "profile");
		String nickname = profile == null ? null : asString(profile.get("nickname"));

		return new KakaoOAuth2User(
			user.getAuthorities(),
			attributes,
			userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(),
			idNumber.longValue(),
			nickname
		);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMap(Map<String, Object> source, String key) {
		Object value = source.get(key);
		if (value instanceof Map<?, ?> map) {
			return (Map<String, Object>)map;
		}
		return null;
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
