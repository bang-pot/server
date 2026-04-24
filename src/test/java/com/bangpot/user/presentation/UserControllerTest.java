package com.bangpot.user.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.time.Instant;

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
import com.bangpot.explore.domain.view.MyFavoriteThemesSummaryView;
import com.bangpot.explore.domain.view.MyFavoriteThemesView;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyMeetingLogsUseCase;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;
import com.bangpot.user.application.exception.WithdrawalNotAllowedException;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.MyPendingCrewsView;
import com.bangpot.meeting.domain.MeetingResult;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.user.domain.view.MyProfileView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;
import com.bangpot.user.domain.view.UserSearchView;

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
	private GetMyFavoriteThemesSummaryUseCase getMyFavoriteThemesSummaryUseCase;

	@MockitoBean
	private GetMyFavoriteThemesUseCase getMyFavoriteThemesUseCase;

	@MockitoBean
	private GetMyMeetingLogsUseCase getMyMeetingLogsUseCase;

	@MockitoBean
	private GetMyJoinedMeetingsUseCase getMyJoinedMeetingsUseCase;

	@MockitoBean
	private GetMyCrewsUseCase getMyCrewsUseCase;

	@MockitoBean
	private GetMyPendingCrewsUseCase getMyPendingCrewsUseCase;

	@MockitoBean
	private GetMyWithdrawalCheckUseCase getMyWithdrawalCheckUseCase;

	@MockitoBean
	private SearchUsersUseCase searchUsersUseCase;

	@MockitoBean
	private WithdrawMyAccountUseCase withdrawMyAccountUseCase;

	@MockitoBean
	private AuthCookieFactory authCookieFactory;

	@MockitoBean
	private UpdateMyProfileUseCase updateMyProfileUseCase;

	@Test
	void returnsCurrentProfileFromNewUserPath() throws Exception {
		when(getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(77L)))
			.thenReturn(MyProfileView.of(77L, "bangpot", null, 4L, 3L, 2L, 1L));

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
			.thenReturn(MyCreatedMeetingsView.of(
				List.of(
					MyCreatedMeetingsView.Item.of(
						101L,
						"Friday Escape",
						MeetingStatus.COMPLETED,
						"2026-04-17",
						"19:00",
						5L,
						"Room Escape Crew"
					)
				),
				MyCreatedMeetingsView.Page.of(0, 20, false)
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
			.thenReturn(MyCalendarView.of(
				List.of(
					MyCalendarView.Item.of(
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
					MyCalendarView.Item.of(
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
	void returnsMyFavoriteThemesSummaryFromNewUserPath() throws Exception {
		when(getMyFavoriteThemesSummaryUseCase.handle(GetMyFavoriteThemesSummaryUseCase.Query.of(77L)))
			.thenReturn(MyFavoriteThemesSummaryView.of(
				List.of(
					MyFavoriteThemesSummaryView.Item.of(
						901L,
						"Deep Blue",
						"Seoul Escape",
						"서울",
						"https://cdn.example.com/theme-901.jpg",
						12,
						true
					)
				),
				6L,
				true
			));

		mockMvc.perform(
			get("/api/users/me/favorites/summary")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].themeId").value(901))
			.andExpect(jsonPath("$.items[0].themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.items[0].storeName").value("Seoul Escape"))
			.andExpect(jsonPath("$.items[0].regionName").value("서울"))
			.andExpect(jsonPath("$.items[0].thumbnailUrl").value("https://cdn.example.com/theme-901.jpg"))
			.andExpect(jsonPath("$.items[0].favoriteCount").value(12))
			.andExpect(jsonPath("$.items[0].isFavorite").value(true))
			.andExpect(jsonPath("$.totalCount").value(6))
			.andExpect(jsonPath("$.hasMore").value(true));
	}

	@Test
	void returnsMyFavoriteThemesFromNewUserPath() throws Exception {
		when(getMyFavoriteThemesUseCase.handle(GetMyFavoriteThemesUseCase.Query.of(77L, 0, 20)))
			.thenReturn(MyFavoriteThemesView.of(
				List.of(
					MyFavoriteThemesView.Item.of(
						901L,
						"Deep Blue",
						"Seoul Escape",
						"서울",
						"https://cdn.example.com/theme-901.jpg",
						12,
						true
					)
				),
				MyFavoriteThemesView.Page.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/users/me/favorites")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].themeId").value(901))
			.andExpect(jsonPath("$.items[0].themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.items[0].storeName").value("Seoul Escape"))
			.andExpect(jsonPath("$.items[0].regionName").value("서울"))
			.andExpect(jsonPath("$.items[0].thumbnailUrl").value("https://cdn.example.com/theme-901.jpg"))
			.andExpect(jsonPath("$.items[0].favoriteCount").value(12))
			.andExpect(jsonPath("$.items[0].isFavorite").value(true))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsMyMeetingLogsFromNewUserPath() throws Exception {
		when(getMyMeetingLogsUseCase.handle(GetMyMeetingLogsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(MyMeetingLogsView.of(
				List.of(
					MyMeetingLogsView.Item.of(
						501L,
						31L,
						"Alpha Crew",
						201L,
						"Friday Escape",
						"2026-04-18",
						java.time.Instant.parse("2026-04-19T10:15:30Z"),
						"Too fun to stop writing",
						"https://cdn.example.com/log-cover.jpg",
						3L
					)
				),
				MyMeetingLogsView.Page.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/users/me/logs")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].logId").value(501))
			.andExpect(jsonPath("$.items[0].crewId").value(31))
			.andExpect(jsonPath("$.items[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.items[0].meetingId").value(201))
			.andExpect(jsonPath("$.items[0].meetingTitle").value("Friday Escape"))
			.andExpect(jsonPath("$.items[0].meetingDate").value("2026-04-18"))
			.andExpect(jsonPath("$.items[0].createdAt").value("2026-04-19T10:15:30Z"))
			.andExpect(jsonPath("$.items[0].excerpt").value("Too fun to stop writing"))
			.andExpect(jsonPath("$.items[0].coverPhotoUrl").value("https://cdn.example.com/log-cover.jpg"))
			.andExpect(jsonPath("$.items[0].photoCount").value(3))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsMyJoinedMeetingsFromNewUserPath() throws Exception {
		when(getMyJoinedMeetingsUseCase.handle(GetMyJoinedMeetingsUseCase.Query.of(77L, 0, 20)))
			.thenReturn(MyJoinedMeetingsView.of(
				List.of(
					MyJoinedMeetingsView.Item.of(
						201L,
						"Sunday Escape",
						"Deep Blue",
						5L,
						"Room Escape Crew",
						"2026-04-18",
						"19:00",
						MeetingStatus.COMPLETED,
						MeetingResult.SUCCESS,
						true
					)
				),
				MyJoinedMeetingsView.Page.of(0, 20, false)
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
			.thenReturn(MyCrewsView.of(
				List.of(
					MyCrewsView.Item.of(
						31L,
						"Alpha Crew",
						CrewVisibility.PUBLIC,
						"leader-pot",
						"https://cdn.example.com/crew-alpha.jpg"
					)
				),
				MyCrewsView.Page.of(0, 20, false)
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
			.thenReturn(MyPendingCrewsView.of(
				List.of(
					MyPendingCrewsView.Item.of(
						101L,
						31L,
						"Alpha Crew",
						"2026-04-17T09:30:00Z",
						"I want to join this crew"
					)
				),
				MyPendingCrewsView.Page.of(0, 20, false)
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
	void returnsUserSearchResultsFromNewUserPath() throws Exception {
		when(searchUsersUseCase.handle(SearchUsersUseCase.Query.of(77L, "pot", 0, 20)))
			.thenReturn(UserSearchView.of(
				List.of(
					UserSearchView.Item.of(
						101L,
						"bangpot",
						"https://cdn.example.com/users/101.jpg",
						"escape lover",
						"MALE",
						0
					)
				),
				UserSearchView.Page.of(0, 20, 1L, 1)
			));

		mockMvc.perform(
			get("/api/users/search")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("keyword", "pot")
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].userId").value(101))
			.andExpect(jsonPath("$.items[0].nickname").value("bangpot"))
			.andExpect(jsonPath("$.items[0].profileImageUrl").value("https://cdn.example.com/users/101.jpg"))
			.andExpect(jsonPath("$.items[0].bio").value("escape lover"))
			.andExpect(jsonPath("$.items[0].gender").value("MALE"))
			.andExpect(jsonPath("$.items[0].escapeCount").value(0))
			.andExpect(jsonPath("$.pageInfo.page").value(0))
			.andExpect(jsonPath("$.pageInfo.size").value(20))
			.andExpect(jsonPath("$.pageInfo.totalElements").value(1))
			.andExpect(jsonPath("$.pageInfo.totalPages").value(1));
	}

	@Test
	void returnsMyWithdrawalCheckFromNewUserPath() throws Exception {
		when(getMyWithdrawalCheckUseCase.handle(GetMyWithdrawalCheckUseCase.Query.of(77L)))
			.thenReturn(MyWithdrawalCheckView.of(
				false,
				List.of(
					MyWithdrawalCheckView.BlockingActiveCrew.of(31L, "Alpha Crew")
				)
			));

		mockMvc.perform(
			get("/api/users/me/withdrawal-check")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.canWithdraw").value(false))
			.andExpect(jsonPath("$.blockingActiveCrews[0].crewId").value(31))
			.andExpect(jsonPath("$.blockingActiveCrews[0].crewName").value("Alpha Crew"));
	}

	@Test
	void withdrawsCurrentUserAndClearsAuthCookie() throws Exception {
		when(withdrawMyAccountUseCase.handle(
			WithdrawMyAccountUseCase.Command.of(
				77L,
				com.bangpot.user.domain.WithdrawalReasonCode.OTHER,
				null
			)
		)).thenReturn(WithdrawMyAccountUseCase.Result.of(Instant.parse("2026-04-17T10:15:30Z"), false));
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
	void returnsValidationErrorWhenWithdrawalReasonDetailExceedsMaximumLength() throws Exception {
		String tooLongReasonDetail = "a".repeat(501);

		mockMvc.perform(
			post("/api/users/me/withdrawal")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "reasonCode": "OTHER",
					  "reasonDetail": "%s",
					  "confirmationChecked": true
					}
					""".formatted(tooLongReasonDetail))
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("reasonDetail"));
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
	void returnsUnauthorizedWhenMeetingLogsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/logs"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsUnauthorizedWhenFavoriteSummaryRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/favorites/summary"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsUnauthorizedWhenUserSearchRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/search").param("keyword", "pot"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void returnsUnauthorizedWhenFavoritesRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/me/favorites"))
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
	void returnsValidationErrorWhenMeetingLogsPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/users/me/logs")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsValidationErrorWhenFavoritesPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/users/me/favorites")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsValidationErrorWhenUserSearchSizeExceedsMaximum() throws Exception {
		mockMvc.perform(
			get("/api/users/search")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.param("keyword", "pot")
				.param("size", "51")
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
			.andExpect(status().isNoContent());
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
