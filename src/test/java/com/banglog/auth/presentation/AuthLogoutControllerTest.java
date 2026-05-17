package com.banglog.auth.presentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import com.banglog.auth.application.usecase.CompleteTempUserUseCase;
import com.banglog.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.banglog.auth.application.usecase.LogoutUseCase;
import com.banglog.common.error.ApiErrorResponseFactory;
import com.banglog.common.error.GlobalApiExceptionHandler;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {AuthLogoutController.class, AuthController.class})
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class AuthLogoutControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LogoutUseCase logoutUseCase;

	@MockitoBean
	private AuthCookieFactory authCookieFactory;

	@MockitoBean
	private GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;

	@MockitoBean
	private CompleteTempUserUseCase completeTempUserUseCase;

	@Test
	void delegatesLogoutActionAndWritesExpiredCookie() throws Exception {
		when(authCookieFactory.createLogoutCookieHeader()).thenReturn(
			"access_token=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax"
		);

		mockMvc.perform(post("/api/auth/logout")
			.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of())))
			.andExpect(status().isNoContent())
			.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("access_token=")))
			.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

		verify(logoutUseCase).handle(LogoutUseCase.Command.of(77L));
		verify(authCookieFactory).createLogoutCookieHeader();
	}

	@Test
	void returnsGuestAuthStateWhenMeIsRequestedAfterLogout() throws Exception {
		when(authCookieFactory.createLogoutCookieHeader()).thenReturn(
			"access_token=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax"
		);
		when(getCurrentAuthUserUseCase.handle(GetCurrentAuthUserUseCase.Query.of(null)))
			.thenReturn(GetCurrentAuthUserUseCase.View.guest());

		mockMvc.perform(post("/api/auth/logout")
			.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of())))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authStatus").value("GUEST"))
			.andExpect(jsonPath("$.completionRequired").value(false))
			.andExpect(jsonPath("$.requiredTermsVersion").isEmpty())
			.andExpect(jsonPath("$.user").doesNotExist());
	}
}
