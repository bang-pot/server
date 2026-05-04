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
import com.bangpot.meeting.application.exception.MeetingGalleryNotFoundException;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MeetingGalleryController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class MeetingGalleryDetailControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetCrewMeetingGalleryUseCase getCrewMeetingGalleryUseCase;

	@MockitoBean
	private GetCrewMeetingGalleryDetailUseCase getCrewMeetingGalleryDetailUseCase;

	@Test
	void returnsMeetingGalleryDetailForAuthenticatedUser() throws Exception {
		when(getCrewMeetingGalleryDetailUseCase.handle(GetCrewMeetingGalleryDetailUseCase.Query.of(5L, 55L, 7L)))
			.thenReturn(CrewMeetingGalleryDetailView.of(
				55L,
				"2026-04-12",
				"금요일 이스케이프",
				List.of(
					CrewMeetingGalleryDetailView.Photo.of(501L, "https://cdn.example.com/a.jpg", 1),
					CrewMeetingGalleryDetailView.Photo.of(502L, "https://cdn.example.com/b.jpg", 2)
				),
				2
			));

		mockMvc.perform(
			get("/api/crews/5/gallery/55")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.meetingId").value(55))
			.andExpect(jsonPath("$.meetingDate").value("2026-04-12"))
			.andExpect(jsonPath("$.meetingTitle").value("금요일 이스케이프"))
			.andExpect(jsonPath("$.photos[0].photoId").value(501))
			.andExpect(jsonPath("$.photos[0].url").value("https://cdn.example.com/a.jpg"))
			.andExpect(jsonPath("$.photos[0].order").value(1))
			.andExpect(jsonPath("$.totalPhotoCount").value(2));
	}

	@Test
	void returnsUnauthorizedWhenDetailRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/crews/5/gallery/55"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsGalleryNotFoundWhenMeetingIsNotGalleryTarget() throws Exception {
		when(getCrewMeetingGalleryDetailUseCase.handle(GetCrewMeetingGalleryDetailUseCase.Query.of(5L, 999L, 7L)))
			.thenThrow(new MeetingGalleryNotFoundException(999L));

		mockMvc.perform(
			get("/api/crews/5/gallery/999")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("GALLERY_NOT_FOUND"));
	}
}
