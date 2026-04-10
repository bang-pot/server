package com.bangpot.user.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UserController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;

	@MockitoBean
	private GetMyProfileUseCase getMyProfileUseCase;

	@MockitoBean
	private UpdateMyProfileUseCase updateMyProfileUseCase;

	@Test
	void returnsCurrentProfileFromNewUserPath() throws Exception {
		when(getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(77L)))
			.thenReturn(GetMyProfileUseCase.View.of(77L, "bangpot"));

		mockMvc.perform(
			get("/api/users/me")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(77))
			.andExpect(jsonPath("$.nickname").value("bangpot"));
	}

	@Test
	void returnsNicknameAvailabilityFromNewUserPath() throws Exception {
		when(checkNicknameAvailabilityUseCase.handle(CheckNicknameAvailabilityUseCase.Query.of("bang")))
			.thenReturn(CheckNicknameAvailabilityUseCase.Result.of("bang", true));

		mockMvc.perform(get("/api/users/nickname-availability").param("nickname", "bang"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nickname").value("bang"))
			.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	void returnsUnauthorizedWhenProfileUpdateIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			patch("/api/users/me")
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
	void updatesCurrentProfileFromNewUserPath() throws Exception {
		when(updateMyProfileUseCase.handle(UpdateMyProfileUseCase.Command.of(77L, "new-pot")))
			.thenReturn(UpdateMyProfileUseCase.Result.of(77L, "new-pot"));

		mockMvc.perform(
			patch("/api/users/me")
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
			patch("/api/users/me")
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
}
