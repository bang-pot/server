package com.bangpot.auth.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.auth.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;

	@MockitoBean
	private CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;

	@MockitoBean
	private CompleteTempUserUseCase completeTempUserUseCase;

	@Test
	void returnsGuestStatusWhenNoAuthenticatedUserExists() throws Exception {
		when(getCurrentAuthUserUseCase.handle(GetCurrentAuthUserUseCase.Query.of(null)))
			.thenReturn(GetCurrentAuthUserUseCase.View.guest("2026-03-25"));

		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authStatus").value("GUEST"))
			.andExpect(jsonPath("$.completionRequired").value(false))
			.andExpect(jsonPath("$.user").doesNotExist());
	}

	@Test
	void returnsTempStatusWithCompletionRoutingHints() throws Exception {
		when(getCurrentAuthUserUseCase.handle(GetCurrentAuthUserUseCase.Query.of(55L))).thenReturn(
			GetCurrentAuthUserUseCase.View.authenticated(
			GetCurrentAuthUserUseCase.AuthStatus.TEMP,
			true,
			"/protected-demo",
			"2026-03-25",
			GetCurrentAuthUserUseCase.AuthenticatedUserView.of(55L, null),
			null
		));

		mockMvc.perform(
			get("/api/auth/me")
				.header(HttpHeaders.COOKIE, "BANGPOT_ACCESS_TOKEN=test")
				.principal(new UsernamePasswordAuthenticationToken(55L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authStatus").value("TEMP"))
			.andExpect(jsonPath("$.completionRequired").value(true))
			.andExpect(jsonPath("$.redirectTo").value("/protected-demo"))
			.andExpect(jsonPath("$.requiredTermsVersion").value("2026-03-25"))
			.andExpect(jsonPath("$.user.id").value(55));
	}

	@Test
	void returnsFullStatusForCompletedUser() throws Exception {
		when(getCurrentAuthUserUseCase.handle(GetCurrentAuthUserUseCase.Query.of(77L))).thenReturn(
			GetCurrentAuthUserUseCase.View.authenticated(
			GetCurrentAuthUserUseCase.AuthStatus.FULL,
			false,
			null,
			"2026-03-25",
			GetCurrentAuthUserUseCase.AuthenticatedUserView.of(77L, "bangpot"),
			Instant.parse("2026-03-31T00:00:00Z")
		));

		mockMvc.perform(
			get("/api/auth/me")
				.header(HttpHeaders.COOKIE, "BANGPOT_ACCESS_TOKEN=test")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authStatus").value("FULL"))
			.andExpect(jsonPath("$.completionRequired").value(false))
			.andExpect(jsonPath("$.user.nickname").value("bangpot"))
			.andExpect(jsonPath("$.requiredTermsAcceptedAt").value("2026-03-31T00:00:00Z"));
	}

	@Test
	void returnsUnauthorizedWhenCompletionIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			post("/api/auth/complete")
				.contentType("application/json")
				.content("""
					{
					  "nickname": "bangpot",
					  "agreedToRequiredTerms": true
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.message").value("인증이 필요합니다."));
	}

	@Test
	void returnsKoreanValidationMessageWhenNicknameIsBlank() throws Exception {
		mockMvc.perform(
			post("/api/auth/complete")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "nickname": "   ",
					  "agreedToRequiredTerms": true
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("AUTH_BAD_REQUEST"))
			.andExpect(jsonPath("$.message").value("닉네임은 비어 있을 수 없습니다."));
	}
}
