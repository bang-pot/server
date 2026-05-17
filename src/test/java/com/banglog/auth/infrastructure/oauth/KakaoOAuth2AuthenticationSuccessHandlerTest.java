package com.banglog.auth.infrastructure.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.banglog.auth.application.usecase.LoginWithProviderUseCase;
import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.infrastructure.AuthSessionTokenService;
import com.banglog.auth.infrastructure.config.AuthFrontendProperties;
import com.banglog.auth.infrastructure.config.AuthJwtProperties;
import com.banglog.common.security.oauth.KakaoAuthorizationRequestResolver;

class KakaoOAuth2AuthenticationSuccessHandlerTest {

	private LoginWithProviderUseCase loginWithProviderUseCase;
	private AuthSessionTokenService authSessionTokenService;
	private AuthJwtProperties authJwtProperties;
	private AuthFrontendProperties authFrontendProperties;
	private KakaoOAuth2AuthenticationSuccessHandler handler;
	private HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository;

	@BeforeEach
	void setUp() {
		loginWithProviderUseCase = org.mockito.Mockito.mock(LoginWithProviderUseCase.class);
		authSessionTokenService = org.mockito.Mockito.mock(AuthSessionTokenService.class);
		authJwtProperties = new AuthJwtProperties();
		authJwtProperties.setCookieName("access_token");
		authJwtProperties.setAccessTokenValiditySeconds(604800);
		authJwtProperties.setSecureCookie(false);
		authFrontendProperties = new AuthFrontendProperties();
		authFrontendProperties.setBaseUrl("http://localhost:3000");
		authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

		handler = new KakaoOAuth2AuthenticationSuccessHandler(
			loginWithProviderUseCase,
			authSessionTokenService,
			authJwtProperties,
			authFrontendProperties,
			authorizationRequestRepository
		);
	}

	@Test
	void redirectsNewTempUserToFrontendCompletionAndIssuesJwtCookie() throws Exception {
		when(loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2002", "/protected-demo")
		))
			.thenReturn(LoginWithProviderUseCase.Result.temp(7L, "/auth/complete", "/protected-demo"));
		when(authSessionTokenService.createAccessToken(7L)).thenReturn("jwt-token");

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setParameter("state", "state");
		authorizationRequestRepository.saveAuthorizationRequest(
			OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri("https://kauth.kakao.com/oauth/authorize")
				.clientId("client-id")
				.state("state")
				.redirectUri("http://localhost:8080/login/oauth2/code/kakao")
				.authorizationRequestUri("http://localhost:8080/oauth2/authorization/kakao?redirectTo=%2Fprotected-demo")
				.attributes(attributes -> attributes.put(
					KakaoAuthorizationRequestResolver.REDIRECT_TO_ATTRIBUTE,
					"/protected-demo"
				))
				.build(),
			request,
			response
		);
		KakaoOAuth2User principal = new KakaoOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Map.of("id", 2002L),
			"id",
			2002L,
			"kakao-temp"
		);

		handler.onAuthenticationSuccess(
			request,
			response,
			new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
		);

		verify(loginWithProviderUseCase).handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2002", "/protected-demo")
		);
		assertThat(response.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/auth/complete?redirectTo=%2Fprotected-demo");
		assertThat(response.getHeader("Set-Cookie")).contains("access_token=jwt-token");
	}

	@Test
	void redirectsExistingFullUserToRequestedFrontendPath() throws Exception {
		when(loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "1001", "/protected-demo")
		))
			.thenReturn(LoginWithProviderUseCase.Result.full(1L, "/protected-demo"));
		when(authSessionTokenService.createAccessToken(1L)).thenReturn("jwt-token");

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setParameter("state", "state");
		authorizationRequestRepository.saveAuthorizationRequest(
			OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri("https://kauth.kakao.com/oauth/authorize")
				.clientId("client-id")
				.state("state")
				.redirectUri("http://localhost:8080/login/oauth2/code/kakao")
				.authorizationRequestUri("http://localhost:8080/oauth2/authorization/kakao?redirectTo=%2Fprotected-demo")
				.attributes(attributes -> attributes.put(
					KakaoAuthorizationRequestResolver.REDIRECT_TO_ATTRIBUTE,
					"/protected-demo"
				))
				.build(),
			request,
			response
		);
		KakaoOAuth2User principal = new KakaoOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Map.of("id", 1001L),
			"id",
			1001L,
			"kakao-full"
		);

		handler.onAuthenticationSuccess(
			request,
			response,
			new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
		);

		assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:3000/protected-demo");
		assertThat(response.getHeaders("Set-Cookie")).hasSize(1);
	}
}
