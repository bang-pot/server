package com.banglog.meeting.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.banglog.common.error.ApiErrorResponseFactory;
import com.banglog.common.error.GlobalApiExceptionHandler;
import com.banglog.meeting.application.usecase.CreateMeetingUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingUseCase;
import com.banglog.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.CompleteMeetingUseCase;
import com.banglog.meeting.application.usecase.GetMeetingDetailUseCase;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.application.usecase.JoinMeetingUseCase;
import com.banglog.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.UpdateMeetingUseCase;
import com.banglog.meeting.domain.view.MeetingDetailView;
import com.banglog.meeting.domain.view.MeetingsView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MeetingController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class MeetingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateMeetingUseCase createMeetingUseCase;

	@MockitoBean
	private GetMeetingsUseCase getMeetingsUseCase;

	@MockitoBean
	private GetMeetingDetailUseCase getMeetingDetailUseCase;

	@MockitoBean
	private JoinMeetingUseCase joinMeetingUseCase;

	@MockitoBean
	private CancelMeetingParticipationUseCase cancelMeetingParticipationUseCase;

	@MockitoBean
	private CloseMeetingRecruitmentUseCase closeMeetingRecruitmentUseCase;

	@MockitoBean
	private ReopenMeetingRecruitmentUseCase reopenMeetingRecruitmentUseCase;

	@MockitoBean
	private CancelMeetingUseCase cancelMeetingUseCase;

	@MockitoBean
	private CompleteMeetingUseCase completeMeetingUseCase;

	@MockitoBean
	private UpdateMeetingUseCase updateMeetingUseCase;

	@Test
	void createsMeetingForJoinedCrewMember() throws Exception {
		when(createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			1L,
			77L,
			"Friday Escape",
			"2026-04-20",
			"19:30",
			"Gangnam",
			"Time Attack",
			4,
			120000,
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		))).thenReturn(CreateMeetingUseCase.Result.of(
			10L,
			1L,
			"Friday Escape",
			"Time Attack",
			"Gangnam",
			"2026-04-20",
			"19:30",
			"RECRUITING"
		));

		mockMvc.perform(
			post("/api/crews/1/meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "title": "Friday Escape",
					  "date": "2026-04-20",
					  "time": "19:30",
					  "place": "Gangnam",
					  "themeName": "Time Attack",
					  "capacity": 4,
					  "totalCost": 120000,
					  "contactLink": "https://open.kakao.com/o/abc123",
					  "description": "Please arrive on time"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.title").value("Friday Escape"))
			.andExpect(jsonPath("$.status").value("RECRUITING"));
	}

	@Test
	void joinsMeetingImmediately() throws Exception {
		when(joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(1L, 10L, 77L)
		)).thenReturn(JoinMeetingUseCase.Result.of(10L, "JOINED"));

		mockMvc.perform(
			post("/api/crews/1/meetings/10/join")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.myParticipationStatus").value("JOINED"));
	}

	@Test
	void cancelsJoinedMeetingParticipation() throws Exception {
		when(cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(1L, 10L, 77L)
		)).thenReturn(CancelMeetingParticipationUseCase.Result.of(10L, "NOT_JOINED"));

		mockMvc.perform(
			delete("/api/crews/1/meetings/10/join")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.myParticipationStatus").value("NOT_JOINED"));
	}

	@Test
	void closesMeetingRecruitment() throws Exception {
		when(closeMeetingRecruitmentUseCase.handle(
			CloseMeetingRecruitmentUseCase.Command.of(1L, 10L, 77L)
		)).thenReturn(CloseMeetingRecruitmentUseCase.Result.of(10L, "RECRUITMENT_CLOSED"));

		mockMvc.perform(
			post("/api/crews/1/meetings/10/close-recruitment")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.status").value("RECRUITMENT_CLOSED"));
	}

	@Test
	void reopensMeetingRecruitment() throws Exception {
		when(reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(1L, 10L, 77L)
		)).thenReturn(ReopenMeetingRecruitmentUseCase.Result.of(10L, "RECRUITING"));

		mockMvc.perform(
			post("/api/crews/1/meetings/10/reopen-recruitment")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.status").value("RECRUITING"));
	}

	@Test
	void cancelsMeeting() throws Exception {
		when(cancelMeetingUseCase.handle(
			CancelMeetingUseCase.Command.of(1L, 10L, 77L)
		)).thenReturn(CancelMeetingUseCase.Result.of(10L, "CANCELED"));

		mockMvc.perform(
			post("/api/crews/1/meetings/10/cancel")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.status").value("CANCELED"));
	}

	@Test
	void completesMeeting() throws Exception {
		when(completeMeetingUseCase.handle(
			CompleteMeetingUseCase.Command.of(1L, 10L, 77L)
		)).thenReturn(CompleteMeetingUseCase.Result.of(10L, "COMPLETED"));

		mockMvc.perform(
			post("/api/crews/1/meetings/10/complete")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	void updatesRecruitingMeetingForHost() throws Exception {
		when(updateMeetingUseCase.handle(
			UpdateMeetingUseCase.Command.of(
				1L,
				10L,
				77L,
				"Late Night Escape",
				"2026-04-22",
				"20:00",
				"Hongdae",
				"Deep Blue",
				2,
				90000,
				"https://open.kakao.com/o/new123",
				"Updated description"
			)
		)).thenReturn(UpdateMeetingUseCase.Result.of(
			10L,
			1L,
			77L,
			"Late Night Escape",
			"Deep Blue",
			"Hongdae",
			"2026-04-22",
			"20:00",
			2,
			90000,
			"https://open.kakao.com/o/new123",
			"Updated description",
			"RECRUITING"
		));

		mockMvc.perform(
			patch("/api/crews/1/meetings/10")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "title": "Late Night Escape",
					  "date": "2026-04-22",
					  "time": "20:00",
					  "place": "Hongdae",
					  "themeName": "Deep Blue",
					  "capacity": 2,
					  "totalCost": 90000,
					  "contactLink": "https://open.kakao.com/o/new123",
					  "description": "Updated description"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.title").value("Late Night Escape"))
			.andExpect(jsonPath("$.themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.place").value("Hongdae"))
			.andExpect(jsonPath("$.date").value("2026-04-22"))
			.andExpect(jsonPath("$.time").value("20:00"))
			.andExpect(jsonPath("$.capacity").value(2))
			.andExpect(jsonPath("$.totalCost").value(90000))
			.andExpect(jsonPath("$.contactLink").value("https://open.kakao.com/o/new123"))
			.andExpect(jsonPath("$.description").value("Updated description"))
			.andExpect(jsonPath("$.status").value("RECRUITING"));
	}

	@Test
	void returnsValidationErrorWhenRequiredMeetingFieldIsMissing() throws Exception {
		mockMvc.perform(
			post("/api/crews/1/meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "date": "2026-04-20",
					  "time": "19:30",
					  "place": "",
					  "themeName": "Time Attack",
					  "title": "Friday Escape",
					  "capacity": 4
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").exists());
	}

	@Test
	void returnsUnauthorizedWhenMeetingCreateIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			post("/api/crews/1/meetings")
				.contentType("application/json")
				.content("""
					{
					  "title": "Friday Escape",
					  "date": "2026-04-20",
					  "time": "19:30",
					  "place": "Gangnam",
					  "themeName": "Time Attack",
					  "capacity": 4
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsUnauthorizedWhenJoinIsSubmittedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/crews/1/meetings/10/join"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsUnauthorizedWhenCancelJoinIsSubmittedWithoutAuthentication() throws Exception {
		mockMvc.perform(delete("/api/crews/1/meetings/10/join"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsUnauthorizedWhenMeetingStatusChangeIsSubmittedWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/crews/1/meetings/10/close-recruitment"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenRequiredMeetingUpdateFieldIsMissing() throws Exception {
		mockMvc.perform(
			patch("/api/crews/1/meetings/10")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "title": "Late Night Escape",
					  "date": "2026-04-22",
					  "time": "20:00",
					  "place": "",
					  "themeName": "Deep Blue",
					  "capacity": 2
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").exists());
	}

	@Test
	void returnsUnauthorizedWhenMeetingUpdateIsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(
			patch("/api/crews/1/meetings/10")
				.contentType("application/json")
				.content("""
					{
					  "title": "Late Night Escape",
					  "date": "2026-04-22",
					  "time": "20:00",
					  "place": "Hongdae",
					  "themeName": "Deep Blue",
					  "capacity": 2
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsMeetingsForJoinedCrewMember() throws Exception {
		when(getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(1L, 77L, 1, 2))).thenReturn(MeetingsView.of(List.of(
			MeetingsView.Item.of(10L, "Friday Escape", "Time Attack", "Gangnam", "2026-04-20", "19:30", "RECRUITING", 2L, 4),
			MeetingsView.Item.of(11L, "Saturday Escape", "Deep Blue", "Hongdae", "2026-04-21", "20:00", "RECRUITING", 4L, 6)
		), MeetingsView.Page.of(1, 2, true)));

		mockMvc.perform(
			get("/api/crews/1/meetings")
				.param("page", "1")
				.param("size", "2")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(10))
			.andExpect(jsonPath("$.items[0].title").value("Friday Escape"))
			.andExpect(jsonPath("$.items[0].themeName").value("Time Attack"))
			.andExpect(jsonPath("$.items[0].status").value("RECRUITING"))
			.andExpect(jsonPath("$.items[0].participantCount").value(2))
			.andExpect(jsonPath("$.items[1].meetingId").value(11))
			.andExpect(jsonPath("$.pageInfo.page").value(1))
			.andExpect(jsonPath("$.pageInfo.size").value(2))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(true));
	}

	@Test
	void returnsMeetingDetailForJoinedCrewMember() throws Exception {
		when(getMeetingDetailUseCase.handle(GetMeetingDetailUseCase.Query.of(1L, 10L, 77L)))
			.thenReturn(MeetingDetailView.of(
				10L,
				1L,
				77L,
				"Friday Escape",
				"Time Attack",
				"Gangnam",
				"2026-04-20",
				"19:30",
				4,
				120000,
				"https://open.kakao.com/o/abc123",
				"Please arrive on time",
				"RECRUITING",
				"NOT_JOINED"
			));

		mockMvc.perform(
			get("/api/crews/1/meetings/10")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.hostUserId").value(77))
			.andExpect(jsonPath("$.title").value("Friday Escape"))
			.andExpect(jsonPath("$.themeName").value("Time Attack"))
			.andExpect(jsonPath("$.status").value("RECRUITING"))
			.andExpect(jsonPath("$.contactLink").value("https://open.kakao.com/o/abc123"))
			.andExpect(jsonPath("$.myParticipationStatus").value("NOT_JOINED"));
	}

	@Test
	void returnsUnauthorizedWhenMeetingsAreRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/1/meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}
}
