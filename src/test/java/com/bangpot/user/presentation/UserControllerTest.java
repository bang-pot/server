package com.bangpot.user.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.auth.presentation.AuthCookieFactory;
import com.bangpot.user.application.usecase.CancelMyPendingCrewJoinRequestUseCase;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.application.exception.WithdrawalNotAllowedException;

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
	private GetMyCalendarUseCase getMyCalendarUseCase;

	@MockitoBean
	private GetMyJoinedMeetingsUseCase getMyJoinedMeetingsUseCase;

	@MockitoBean
	private GetMyCrewsUseCase getMyCrewsUseCase;

	@MockitoBean
	private GetMyPendingCrewsUseCase getMyPendingCrewsUseCase;

	@MockitoBean
	private CancelMyPendingCrewJoinRequestUseCase cancelMyPendingCrewJoinRequestUseCase;

	@MockitoBean
	private GetMyWithdrawalCheckUseCase getMyWithdrawalCheckUseCase;

	@MockitoBean
	private WithdrawMyAccountUseCase withdrawMyAccountUseCase;

	@MockitoBean
	private AuthCookieFactory authCookieFactory;

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
						"Friday Escape",
						"COMPLETED",
						"2026-04-17",
						"19:00",
						5L,
						"Room Escape Crew"
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
			.andExpect(jsonPath("$.items[0].title").value("Friday Escape"))
			.andExpect(jsonPath("$.items[0].status").value("COMPLETED"))
			.andExpect(jsonPath("$.items[0].date").value("2026-04-17"))
			.andExpect(jsonPath("$.items[0].time").value("19:00"))
			.andExpect(jsonPath("$.items[0].crewId").value(5))
			.andExpect(jsonPath("$.items[0].crewName").value("Room Escape Crew"))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsMyCalendarFromNewUserPath() throws Exception {
		when(getMyCalendarUseCase.handle(GetMyCalendarUseCase.Query.of(77L)))
			.thenReturn(GetMyCalendarUseCase.Result.of(
				List.of(
					GetMyCalendarUseCase.Item.of(
						301L,
						"Friday Escape",
						5L,
						"Room Escape Crew",
						"2026-04-20",
						"19:00",
						"RECRUITING",
						false,
						"HOST"
					),
					GetMyCalendarUseCase.Item.of(
						302L,
						"Canceled Escape",
						6L,
						"Another Crew",
						"2026-04-21",
						"20:00",
						"CANCELED",
						true,
						"PARTICIPANT"
					)
				),
				2
			));

		mockMvc.perform(
			get("/api/users/me/calendar")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(301))
			.andExpect(jsonPath("$.items[0].meetingTitle").value("Friday Escape"))
			.andExpect(jsonPath("$.items[0].crewId").value(5))
			.andExpect(jsonPath("$.items[0].crewName").value("Room Escape Crew"))
			.andExpect(jsonPath("$.items[0].date").value("2026-04-20"))
			.andExpect(jsonPath("$.items[0].time").value("19:00"))
			.andExpect(jsonPath("$.items[0].meetingStatus").value("RECRUITING"))
			.andExpect(jsonPath("$.items[0].isCanceled").value(false))
			.andExpect(jsonPath("$.items[0].participationRole").value("HOST"))
			.andExpect(jsonPath("$.items[1].meetingStatus").value("CANCELED"))
			.andExpect(jsonPath("$.items[1].isCanceled").value(true))
			.andExpect(jsonPath("$.items[1].participationRole").value("PARTICIPANT"))
			.andExpect(jsonPath("$.totalCount").value(2));
	}

	@Test
	void returnsMyJoinedMeetingsFromNewUserPath() throws Exception {
		when(getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(GetMyJoinedMeetingsUseCase.Result.of(
				List.of(
					GetMyJoinedMeetingsUseCase.Item.of(
						201L,
						"Sunday Escape",
						"Deep Blue",
						5L,
						"Room Escape Crew",
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
			.andExpect(jsonPath("$.items[0].title").value("Sunday Escape"))
			.andExpect(jsonPath("$.items[0].themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.items[0].crewId").value(5))
			.andExpect(jsonPath("$.items[0].crewName").value("Room Escape Crew"))
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
	void returnsMyCrewsFromNewUserPath() throws Exception {
		when(getMyCrewsUseCase.handle(GetMyCrewsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(GetMyCrewsUseCase.Result.of(
				List.of(
					GetMyCrewsUseCase.Item.of(
						31L,
						"Alpha Crew",
						"PUBLIC",
						"leader-pot",
						"https://cdn.example.com/crew-alpha.jpg"
					)
				),
				GetMyCrewsUseCase.PageInfo.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/users/me/crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].crewId").value(31))
			.andExpect(jsonPath("$.items[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.items[0].visibility").value("PUBLIC"))
			.andExpect(jsonPath("$.items[0].leaderNickname").value("leader-pot"))
			.andExpect(jsonPath("$.items[0].coverImageUrl").value("https://cdn.example.com/crew-alpha.jpg"))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsMyPendingCrewsFromNewUserPath() throws Exception {
		when(getMyPendingCrewsUseCase.handle(GetMyPendingCrewsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(GetMyPendingCrewsUseCase.Result.of(
				List.of(
					GetMyPendingCrewsUseCase.Item.of(
						101L,
						31L,
						"Alpha Crew",
						"2026-04-17T09:30:00Z",
						"I want to join this crew"
					)
				),
				GetMyPendingCrewsUseCase.PageInfo.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/users/me/pending-crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].joinRequestId").value(101))
			.andExpect(jsonPath("$.items[0].crewId").value(31))
			.andExpect(jsonPath("$.items[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.items[0].requestedAt").value("2026-04-17T09:30:00Z"))
			.andExpect(jsonPath("$.items[0].messageSummary").value("I want to join this crew"))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void cancelsMyPendingCrewRequestFromNewUserPath() throws Exception {
		when(cancelMyPendingCrewJoinRequestUseCase.handle(CancelMyPendingCrewJoinRequestUseCase.Command.of(77L, 101L)))
			.thenReturn(CancelMyPendingCrewJoinRequestUseCase.Result.of(101L, 31L));

		mockMvc.perform(
			delete("/api/users/me/pending-crews/101")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.joinRequestId").value(101))
			.andExpect(jsonPath("$.crewId").value(31));
	}

	@Test
	void returnsMyWithdrawalCheckFromNewUserPath() throws Exception {
		when(getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(77L)))
			.thenReturn(GetMyWithdrawalCheckUseCase.Result.of(
				false,
				List.of(
					GetMyWithdrawalCheckUseCase.BlockingActiveCrew.of(31L, "Alpha Crew")
				),
				List.of(
					GetMyWithdrawalCheckUseCase.BlockingParticipatingMeeting.of(
						101L,
						"Friday Escape",
						31L,
						"Alpha Crew",
						"RECRUITING",
						"2026-04-20",
						"19:00",
						"HOST"
					)
				)
			));

		mockMvc.perform(
			get("/api/users/me/withdrawal-check")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.canWithdraw").value(false))
			.andExpect(jsonPath("$.blockingActiveCrews[0].crewId").value(31))
			.andExpect(jsonPath("$.blockingActiveCrews[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].meetingId").value(101))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].meetingTitle").value("Friday Escape"))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].crewId").value(31))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].meetingStatus").value("RECRUITING"))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].date").value("2026-04-20"))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].time").value("19:00"))
			.andExpect(jsonPath("$.blockingParticipatingMeetings[0].participationRole").value("HOST"));
	}

	@Test
	void withdrawsCurrentUserAndClearsAuthCookie() throws Exception {
		when(withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(
				77L,
				com.bangpot.user.domain.WithdrawalReasonCode.OTHER,
				null
			)
		)).thenReturn(WithdrawMyAccountUseCase.Result.of("2026-04-17T10:15:30Z", false));
		when(authCookieFactory.createLogoutCookieHeader()).thenReturn(
			"access_token=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax"
		);

		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": "OTHER",
					  "reasonDetail": " ",
					  "confirmationChecked": true
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
			.andExpect(jsonPath("$.withdrawnAt").value("2026-04-17T10:15:30Z"))
			.andExpect(jsonPath("$.canLogin").value(false));
	}

	@Test
	void returnsValidationErrorWhenWithdrawalReasonCodeIsBlank() throws Exception {
		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": " ",
					  "confirmationChecked": true
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("reasonCode"));
	}

	@Test
	void returnsValidationErrorWhenWithdrawalConfirmationIsUnchecked() throws Exception {
		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": "OTHER",
					  "confirmationChecked": false
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("confirmationChecked"));
	}

	@Test
	void returnsValidationErrorWhenWithdrawalReasonCodeIsInvalid() throws Exception {
		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": "INVALID",
					  "confirmationChecked": true
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("reasonCode"));
	}

	@Test
	void returnsConflictWhenWithdrawalBecomesBlockedAtExecutionTime() throws Exception {
		when(withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(
				77L,
				com.bangpot.user.domain.WithdrawalReasonCode.NOT_USING,
				null
			)
		)).thenThrow(new WithdrawalNotAllowedException());

		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": "NOT_USING",
					  "confirmationChecked": true
					}
					""")
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("AUTH_WITHDRAWAL_NOT_ALLOWED"));
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
	void returnsUnauthorizedWhenCalendarRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/calendar"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsUnauthorizedWhenWithdrawalCheckRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/withdrawal-check"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsUnauthorizedWhenWithdrawalIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": "OTHER",
					  "confirmationChecked": true
					}
					""")
		)
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
	void returnsUnauthorizedWhenMyCrewsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/crews"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsUnauthorizedWhenMyPendingCrewsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/pending-crews"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsValidationErrorWhenMyPendingCrewsPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/users/me/pending-crews")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsUnauthorizedWhenMyPendingCrewCancelRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(delete("/api/users/me/pending-crews/101"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsValidationErrorWhenMyCrewsPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/users/me/crews")
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
