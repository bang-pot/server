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
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MeetingGalleryController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class MeetingGalleryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetCrewMeetingGalleryUseCase getCrewMeetingGalleryUseCase;

	@MockitoBean
	private GetCrewMeetingGalleryDetailUseCase getCrewMeetingGalleryDetailUseCase;

	@Test
	void returnsCrewMeetingGalleryForAuthenticatedUser() throws Exception {
		when(getCrewMeetingGalleryUseCase.handle(GetCrewMeetingGalleryUseCase.Query.of(5L, 7L, 0, 20)))
			.thenReturn(CrewMeetingGalleryView.of(
				List.of(
					CrewMeetingGalleryView.Item.of(
						55L,
						"2026-04-12",
						"금요일 이스케이프",
						"https://cdn.example.com/a.jpg",
						2L
					)
				),
				CrewMeetingGalleryView.Page.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/crews/5/gallery")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(55))
			.andExpect(jsonPath("$.items[0].meetingDate").value("2026-04-12"))
			.andExpect(jsonPath("$.items[0].meetingTitle").value("금요일 이스케이프"))
			.andExpect(jsonPath("$.items[0].coverPhotoUrl").value("https://cdn.example.com/a.jpg"))
			.andExpect(jsonPath("$.items[0].extraPhotoCount").value(2))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsUnauthorizedWhenGalleryRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/5/gallery"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/crews/5/gallery")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}
}
