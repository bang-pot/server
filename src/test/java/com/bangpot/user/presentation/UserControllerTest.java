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
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
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
	private GetMyCreatedMeetingsUseCase getMyCreatedMeetingsUseCase;

	@MockitoBean
	private GetMyJoinedMeetingsUseCase getMyJoinedMeetingsUseCase;

	@MockitoBean
	private UpdateMyProfileUseCase updateMyProfileUseCase;

	@Test
	void returnsCurrentProfileFromNewUserPath() throws Exception {
		when(getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(77L)))
			.thenReturn(GetMyProfileUseCase.View.of(77L, "bangpot", null, 4L, 3L, 2L, 1L));

		mockMvc.perform(
			get("/api/users/me")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(77))
			.andExpect(jsonPath("$.nickname").value("bangpot"))
			.andExpect(jsonPath("$.profileImageUrl").isEmpty())
			.andExpect(jsonPath("$.createdMeetingsCount").value(4))
			.andExpect(jsonPath("$.joinedMeetingsCount").value(3))
			.andExpect(jsonPath("$.myCrewsCount").value(2))
			.andExpect(jsonPath("$.pendingCrewsCount").value(1));
	}

	@Test
	void returnsMyCreatedMeetingsFromNewUserPath() throws Exception {
		when(getMyCreatedMeetingsUseCase.handle(GetMyCreatedMeetingsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(GetMyCreatedMeetingsUseCase.Result.of(
				List.of(
					GetMyCreatedMeetingsUseCase.Item.of(
						101L,
						"금요일 이스케이프",
						"COMPLETED",
						"2026-04-17",
						"19:00",
						5L,
						"방탈출 크루"
					)
				),
				GetMyCreatedMeetingsUseCase.PageInfo.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/users/me/created-meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(101))
			.andExpect(jsonPath("$.items[0].title").value("금요일 이스케이프"))
			.andExpect(jsonPath("$.items[0].status").value("COMPLETED"))
			.andExpect(jsonPath("$.items[0].date").value("2026-04-17"))
			.andExpect(jsonPath("$.items[0].time").value("19:00"))
			.andExpect(jsonPath("$.items[0].crewId").value(5))
			.andExpect(jsonPath("$.items[0].crewName").value("방탈출 크루"))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsMyJoinedMeetingsFromNewUserPath() throws Exception {
		when(getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(GetMyJoinedMeetingsUseCase.Result.of(
				List.of(
					GetMyJoinedMeetingsUseCase.Item.of(
						201L,
						"토요일 방탈",
						"Deep Blue",
						5L,
						"방탈출 크루",
						"2026-04-18",
						"19:00",
						"COMPLETED",
						"SUCCESS",
						true
					)
				),
				GetMyJoinedMeetingsUseCase.PageInfo.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/users/me/joined-meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(201))
			.andExpect(jsonPath("$.items[0].title").value("토요일 방탈"))
			.andExpect(jsonPath("$.items[0].themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.items[0].crewId").value(5))
			.andExpect(jsonPath("$.items[0].crewName").value("방탈출 크루"))
			.andExpect(jsonPath("$.items[0].date").value("2026-04-18"))
			.andExpect(jsonPath("$.items[0].time").value("19:00"))
			.andExpect(jsonPath("$.items[0].status").value("COMPLETED"))
			.andExpect(jsonPath("$.items[0].result").value("SUCCESS"))
			.andExpect(jsonPath("$.items[0].canWriteReview").value(true))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsUnauthorizedWhenJoinedMeetingsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/joined-meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsValidationErrorWhenJoinedMeetingsPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/users/me/joined-meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsUnauthorizedWhenCreatedMeetingsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/created-meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsValidationErrorWhenCreatedMeetingsPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/users/me/created-meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsUnauthorizedWhenProfileIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
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
