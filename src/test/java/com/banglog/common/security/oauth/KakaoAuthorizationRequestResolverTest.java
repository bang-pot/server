package com.banglog.common.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class KakaoAuthorizationRequestResolverTest {

	@Test
	void storesRequestedRedirectPathInAuthorizationRequestAttributes() {
		KakaoAuthorizationRequestResolver resolver = new KakaoAuthorizationRequestResolver(
			new InMemoryRegistrationRepository(kakaoRegistration())
		);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/kakao");
		request.setParameter("redirectTo", "/protected-demo");

		OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

		assertThat(authorizationRequest).isNotNull();
		assertThat(authorizationRequest.getAttributes().get(KakaoAuthorizationRequestResolver.REDIRECT_TO_ATTRIBUTE))
			.isEqualTo("/protected-demo");
	}

	@Test
	void ignoresUnsafeRedirectPath() {
		KakaoAuthorizationRequestResolver resolver = new KakaoAuthorizationRequestResolver(
			new InMemoryRegistrationRepository(kakaoRegistration())
		);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/kakao");
		request.setParameter("redirectTo", "https://evil.example");

		OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

		assertThat(authorizationRequest).isNotNull();
		assertThat(authorizationRequest.getAttributes())
			.doesNotContainKey(KakaoAuthorizationRequestResolver.REDIRECT_TO_ATTRIBUTE);
	}

	private ClientRegistration kakaoRegistration() {
		return ClientRegistration.withRegistrationId("kakao")
			.clientId("client-id")
			.clientSecret("client-secret")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
			.authorizationUri("https://kauth.kakao.com/oauth/authorize")
			.tokenUri("https://kauth.kakao.com/oauth/token")
			.userInfoUri("https://kapi.kakao.com/v2/user/me")
			.userNameAttributeName("id")
			.redirectUri("http://localhost:8080/login/oauth2/code/kakao")
			.build();
	}

	private record InMemoryRegistrationRepository(ClientRegistration registration)
		implements ClientRegistrationRepository {

		@Override
		public ClientRegistration findByRegistrationId(String registrationId) {
			return registration.getRegistrationId().equals(registrationId) ? registration : null;
		}
	}
}
