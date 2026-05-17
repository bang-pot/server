package com.banglog.meeting.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
import com.banglog.meeting.application.usecase.GetCrewMeetingLogFeedUseCase;
import com.banglog.meeting.domain.view.CrewMeetingLogFeedView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MeetingLogFeedController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class MeetingLogFeedControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetCrewMeetingLogFeedUseCase getCrewMeetingLogFeedUseCase;

	@Test
	void returnsCrewMeetingLogFeedForAuthenticatedUser() throws Exception {
		when(getCrewMeetingLogFeedUseCase.handle(GetCrewMeetingLogFeedUseCase.Query.of(5L, 7L, 0, 20)))
			.thenReturn(CrewMeetingLogFeedView.of(
				List.of(
					CrewMeetingLogFeedView.Item.of(
						101L,
						55L,
						"writer",
						"금요일 번개",
						"2026-04-12",
						Instant.parse("2026-04-15T01:00:00Z"),
						"정말 재미있었던 기록입니다.",
						"https://cdn.example.com/a.jpg",
						1L
					)
				),
				CrewMeetingLogFeedView.Page.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/crews/5/logs")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].logId").value(101))
			.andExpect(jsonPath("$.items[0].meetingId").value(55))
			.andExpect(jsonPath("$.items[0].meetingDate").value("2026-04-12"))
			.andExpect(jsonPath("$.items[0].excerpt").value("정말 재미있었던 기록입니다."))
			.andExpect(jsonPath("$.items[0].coverPhotoUrl").value("https://cdn.example.com/a.jpg"))
			.andExpect(jsonPath("$.items[0].extraPhotoCount").value(1))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsUnauthorizedWhenFeedRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/5/logs"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/crews/5/logs")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}
}
