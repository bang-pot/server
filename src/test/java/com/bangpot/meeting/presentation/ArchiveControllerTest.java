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

import com.bangpot.meeting.application.usecase.GetArchivedMeetingsUseCase;
import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ArchiveController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class ArchiveControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetArchivedMeetingsUseCase getArchivedMeetingsUseCase;

	@Test
	void returnsArchiveMeetingsForAuthenticatedUser() throws Exception {
		when(getArchivedMeetingsUseCase.handle(GetArchivedMeetingsUseCase.Query.of(7L, 0, 20)))
			.thenReturn(GetArchivedMeetingsUseCase.Result.of(
				List.of(
					GetArchivedMeetingsUseCase.Item.of(
						31L, 101L, "Alpha Crew", "Deep Blue", "Hongdae", "2026-04-12", "SUCCESS",
						"https://image.example/deep-blue.jpg"
					)
				),
				GetArchivedMeetingsUseCase.PageInfo.of(0, 20, false)
			));

		mockMvc.perform(
			get("/api/archive/meetings")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "0")
				.param("size", "20")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].meetingId").value(31))
			.andExpect(jsonPath("$.items[0].crewId").value(101))
			.andExpect(jsonPath("$.items[0].crewName").value("Alpha Crew"))
			.andExpect(jsonPath("$.items[0].posterImageUrl").value("https://image.example/deep-blue.jpg"))
			.andExpect(jsonPath("$.pageInfo.hasNext").value(false));
	}

	@Test
	void returnsUnauthorizedWhenArchiveMeetingsRequestedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/archive/meetings"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenPageIsNegative() throws Exception {
		mockMvc.perform(
			get("/api/archive/meetings")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.param("page", "-1")
				.param("size", "20")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}
}

