package com.bangpot.auth.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.auth.application.exception.DuplicateNicknameException;
import com.bangpot.auth.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.application.usecase.GetMyProfileUseCase;
import com.bangpot.auth.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;

	@MockitoBean
	private CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;

	@MockitoBean
	private CompleteTempUserUseCase completeTempUserUseCase;

	@MockitoBean
	private GetMyProfileUseCase getMyProfileUseCase;

	@MockitoBean
	private UpdateMyProfileUseCase updateMyProfileUseCase;

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
	void returnsCurrentFullUserProfile() throws Exception {
		when(getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(77L)))
			.thenReturn(GetMyProfileUseCase.View.of(77L, "bangpot"));

		mockMvc.perform(
			get("/api/auth/profile")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(77))
			.andExpect(jsonPath("$.nickname").value("bangpot"));
	}

	@Test
	void returnsUnauthorizedWhenProfileUpdateIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			patch("/api/auth/profile")
				.contentType("application/json")
				.content("""
					{
					  "nickname": "bangpot"
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void updatesCurrentFullUserProfile() throws Exception {
		when(updateMyProfileUseCase.handle(UpdateMyProfileUseCase.Command.of(77L, "new-pot")))
			.thenReturn(UpdateMyProfileUseCase.Result.of(77L, "new-pot"));

		mockMvc.perform(
			patch("/api/auth/profile")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "nickname": "new-pot"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(77))
			.andExpect(jsonPath("$.nickname").value("new-pot"));
	}

	@Test
	void returnsValidationErrorWhenProfileNicknameIsBlank() throws Exception {
		mockMvc.perform(
			patch("/api/auth/profile")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "nickname": "   "
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("nickname"));
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
			.andExpect(jsonPath("$.message").value("인증이 필요합니다."))
			.andExpect(jsonPath("$.requestId").value("na"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
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
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
			.andExpect(jsonPath("$.requestId").value("na"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("nickname"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("닉네임은 비어 있을 수 없습니다."));
	}

	@Test
	void returnsCommonEnvelopeForBusinessFailure() throws Exception {
		when(completeTempUserUseCase.handle(org.mockito.ArgumentMatchers.any()))
			.thenThrow(new DuplicateNicknameException("bangpot"));

		mockMvc.perform(
			post("/api/auth/complete")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "nickname": "bangpot",
					  "agreedToRequiredTerms": true
					}
					""")
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("AUTH_DUPLICATE_NICKNAME"))
			.andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."))
			.andExpect(jsonPath("$.requestId").value("na"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsCommonEnvelopeForInternalFailure() throws Exception {
		when(completeTempUserUseCase.handle(org.mockito.ArgumentMatchers.any()))
			.thenThrow(new RuntimeException("boom"));

		mockMvc.perform(
			post("/api/auth/complete")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "nickname": "bangpot",
					  "agreedToRequiredTerms": true
					}
					""")
		)
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value("COMMON_INTERNAL_ERROR"))
			.andExpect(jsonPath("$.message").value("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."))
			.andExpect(jsonPath("$.requestId").value("na"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}
}
