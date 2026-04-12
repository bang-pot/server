package com.bangpot.meeting.presentation;

import static org.mockito.Mockito.when;
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
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;

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

	@Test
	void createsMeetingForJoinedCrewMember() throws Exception {
		when(createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			1L,
			77L,
			"2026-04-20",
			"19:30",
			"강남점",
			"타임 어택",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"지각 없이 모일 분"
		))).thenReturn(CreateMeetingUseCase.Result.of(
			10L,
			1L,
			"타임 어택",
			"강남점",
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
					  "place": "강남점",
					  "themeName": "타임 어택",
					  "capacity": 4,
					  "totalCost": 120000,
					  "reservationLink": "https://example.com/reserve",
					  "openChatLink": "https://open.kakao.com/o/abc123",
					  "description": "지각 없이 모일 분"
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
					  "themeName": "타임 어택",
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
					  "place": "강남점",
					  "themeName": "타임 어택",
					  "capacity": 4
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsMeetingsForJoinedCrewMember() throws Exception {
		when(getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(1L, 77L))).thenReturn(List.of(
			GetMeetingsUseCase.View.of(10L, "타임 어택", "강남점", "2026-04-20", "19:30", "RECRUITING", "NOT_RECORDED", 4),
			GetMeetingsUseCase.View.of(11L, "딥 블루", "홍대점", "2026-04-21", "20:00", "RECRUITING", "NOT_RECORDED", 6)
		));

		mockMvc.perform(
			get("/api/crews/1/meetings")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].meetingId").value(10))
			.andExpect(jsonPath("$[0].themeName").value("타임 어택"))
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
				"타임 어택",
				"강남점",
				"2026-04-20",
				"19:30",
				4,
				120000,
				"https://example.com/reserve",
				"https://open.kakao.com/o/abc123",
				"지각 없이 모일 분",
				"RECRUITING",
				"NOT_RECORDED"
			));

		mockMvc.perform(
			get("/api/crews/1/meetings/10")
				.principal(new UsernamePasswordAuthenticationToken(77L, null, List.of()))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(10))
			.andExpect(jsonPath("$.crewId").value(1))
			.andExpect(jsonPath("$.hostUserId").value(77))
			.andExpect(jsonPath("$.themeName").value("타임 어택"))
			.andExpect(jsonPath("$.status").value("RECRUITING"))
			.andExpect(jsonPath("$.result").value("NOT_RECORDED"));
	}

	@Test
	void returnsUnauthorizedWhenMeetingsAreRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/1/meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}
}
