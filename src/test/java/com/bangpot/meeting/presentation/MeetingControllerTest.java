package com.bangpot.meeting.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;

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

	@Test
	void createsMeetingForJoinedCrewMember() throws Exception {
		when(createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			1L,
			77L,
			"2026-04-20",
			"19:30",
			"Gangnam",
			"Time Attack",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		))).thenReturn(CreateMeetingUseCase.Result.of(
			10L,
			1L,
			"Time Attack",
			"Gangnam",
			"2026-04-20",
			"19:30",
			"RECRUITING",
			"NOT_RECORDED"
		));

		mockMvc.perform(
			post("/api/crews/1/meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
				.contentType("application/json")
				.content("""
					{
					  "date": "2026-04-20",
					  "time": "19:30",
					  "place": "Gangnam",
					  "themeName": "Time Attack",
					  "capacity": 4,
					  "totalCost": 120000,
					  "reservationLink": "https://example.com/reserve",
					  "openChatLink": "https://open.kakao.com/o/abc123",
					  "description": "Please arrive on time"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.status").value("RECRUITING"))
			.andExpect(jsonPath("$.result").value("NOT_RECORDED"));
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
	void returnsMeetingsForJoinedCrewMember() throws Exception {
		when(getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(1L, 77L))).thenReturn(List.of(
			GetMeetingsUseCase.View.of(10L, "Time Attack", "Gangnam", "2026-04-20", "19:30", "RECRUITING", "NOT_RECORDED", 4),
			GetMeetingsUseCase.View.of(11L, "Deep Blue", "Hongdae", "2026-04-21", "20:00", "RECRUITING", "NOT_RECORDED", 6)
		));

		mockMvc.perform(
			get("/api/crews/1/meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].meetingId").value(10))
			.andExpect(jsonPath("$[0].themeName").value("Time Attack"))
			.andExpect(jsonPath("$[0].status").value("RECRUITING"))
			.andExpect(jsonPath("$[0].result").value("NOT_RECORDED"))
			.andExpect(jsonPath("$[1].meetingId").value(11));
	}

	@Test
	void returnsMeetingDetailForJoinedCrewMember() throws Exception {
		when(getMeetingDetailUseCase.handle(GetMeetingDetailUseCase.Query.of(1L, 10L, 77L)))
			.thenReturn(GetMeetingDetailUseCase.Result.of(
				10L,
				1L,
				77L,
				"Time Attack",
				"Gangnam",
				"2026-04-20",
				"19:30",
				4,
				120000,
				"https://example.com/reserve",
				"https://open.kakao.com/o/abc123",
				"Please arrive on time",
				"RECRUITING",
				"NOT_RECORDED",
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
			.andExpect(jsonPath("$.themeName").value("Time Attack"))
			.andExpect(jsonPath("$.status").value("RECRUITING"))
			.andExpect(jsonPath("$.result").value("NOT_RECORDED"))
			.andExpect(jsonPath("$.myParticipationStatus").value("NOT_JOINED"));
	}

	@Test
	void returnsUnauthorizedWhenMeetingsAreRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/1/meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}
}
