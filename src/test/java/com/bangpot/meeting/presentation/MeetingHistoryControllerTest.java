package com.bangpot.meeting.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.bangpot.meeting.application.usecase.GetCrewMeetingHistoryUseCase;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MeetingHistoryController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class MeetingHistoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetCrewMeetingHistoryUseCase getCrewMeetingHistoryUseCase;

	@Test
	void returnsCrewMeetingHistoryForAuthenticatedUser() throws Exception {
		when(getCrewMeetingHistoryUseCase.handle(GetCrewMeetingHistoryUseCase.Query.of(5L, 7L, 0, 20)))
			.thenReturn(GetCrewMeetingHistoryUseCase.Result.of(
				List.of(
					GetCrewMeetingHistoryUseCase.Item.of(
						31L,
						"금요일 이스케이프",
						"Deep Blue",
						"Hongdae",
						"2026-04-12",
						"SUCCESS",
						"HAS_LOG",
						101L
					)
				),
				GetCrewMeetingHistoryUseCase.PageInfo.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/crews/5/history/meetings")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(31))
			.andExpect(jsonPath("$.items[0].meetingTitle").value("금요일 이스케이프"))
			.andExpect(jsonPath("$.items[0].myLogStatus").value("HAS_LOG"))
			.andExpect(jsonPath("$.items[0].logId").value(101))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsUnauthorizedWhenHistoryRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/5/history/meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/crews/5/history/meetings")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}
}
