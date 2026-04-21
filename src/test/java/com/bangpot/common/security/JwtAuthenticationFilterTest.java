package com.bangpot.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.auth.infrastructure.AuthSessionTokenService;
import com.bangpot.auth.infrastructure.config.AuthJwtProperties;

class JwtAuthenticationFilterTest {

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authenticatesWhenTokenAndAuthUserExist() throws Exception {
		AuthSessionTokenService authSessionTokenService = Mockito.mock(AuthSessionTokenService.class);
		AuthUserRepository authUserRepository = Mockito.mock(AuthUserRepository.class);
		when(authSessionTokenService.resolveUserId("token")).thenReturn(Optional.of(77L));
		when(authUserRepository.findById(77L)).thenReturn(Optional.of(fullUser(77L)));

		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
			authJwtProperties(),
			authSessionTokenService,
			authUserRepository
		);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token");

		filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(77L);
	}

	@Test
	void skipsAuthenticationWhenAuthUserWasDeleted() throws Exception {
		AuthSessionTokenService authSessionTokenService = Mockito.mock(AuthSessionTokenService.class);
		AuthUserRepository authUserRepository = Mockito.mock(AuthUserRepository.class);
		when(authSessionTokenService.resolveUserId("token")).thenReturn(Optional.of(77L));
		when(authUserRepository.findById(77L)).thenReturn(Optional.empty());

		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
			authJwtProperties(),
			authSessionTokenService,
			authUserRepository
		);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token");

		filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	private AuthJwtProperties authJwtProperties() {
		AuthJwtProperties properties = new AuthJwtProperties();
		properties.setCookieName("access_token");
		return properties;
	}

	private AuthUser fullUser(Long id) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			"provider-" + id,
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-04-14", java.time.Instant.parse("2026-04-14T00:00:00Z")),
			null,
			java.time.Instant.parse("2026-04-14T00:00:00Z"),
			java.time.Instant.parse("2026-04-14T00:00:00Z")
		);
	}
}
