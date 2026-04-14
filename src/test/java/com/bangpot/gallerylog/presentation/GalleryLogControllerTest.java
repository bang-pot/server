package com.bangpot.gallerylog.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.gallerylog.application.exception.MeetingLogNotFoundException;
import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.gallerylog.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.UpdateMeetingLogUseCase;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = GalleryLogController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class GalleryLogControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateMeetingLogUseCase createMeetingLogUseCase;

	@MockitoBean
	private UpdateMeetingLogUseCase updateMeetingLogUseCase;

	@MockitoBean
	private DeleteMeetingLogUseCase deleteMeetingLogUseCase;

	@MockitoBean
	private GetMyMeetingLogUseCase getMyMeetingLogUseCase;

	@MockitoBean
	private GetMeetingLogDetailUseCase getMeetingLogDetailUseCase;

	@Test
	void createsMeetingLogForAuthenticatedUser() throws Exception {
		when(createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			55L,
			7L,
			"방탈출 기록입니다.",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		))).thenReturn(CreateMeetingLogUseCase.Result.of(101L, 55L));

		mockMvc.perform(
			post("/api/meetings/55/logs")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "body": "방탈출 기록입니다.",
					  "photos": [
					    { "url": "https://cdn.example.com/a.jpg", "sizeBytes": 1024 }
					  ]
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.logId").value(101))
			.andExpect(jsonPath("$.meetingId").value(55));
	}

	@Test
	void updatesMeetingLogForAuthor() throws Exception {
		when(updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			101L,
			7L,
			"수정한 본문",
			List.of(UpdateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/b.png", 2048L))
		))).thenReturn(UpdateMeetingLogUseCase.Result.of(101L, 55L));

		mockMvc.perform(
			patch("/api/logs/101")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "body": "수정한 본문",
					  "photos": [
					    { "url": "https://cdn.example.com/b.png", "sizeBytes": 2048 }
					  ]
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.logId").value(101))
			.andExpect(jsonPath("$.meetingId").value(55));
	}

	@Test
	void deletesMeetingLogForAuthor() throws Exception {
		when(deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(101L, 7L)))
			.thenReturn(DeleteMeetingLogUseCase.Result.of(101L));

		mockMvc.perform(
			delete("/api/logs/101")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.logId").value(101));
	}

	@Test
	void returnsMyMeetingLogDetail() throws Exception {
		when(getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(55L, 7L)))
			.thenReturn(GetMyMeetingLogUseCase.Result.of(
				101L,
				55L,
				"심야 이스케이프",
				"Deep Blue",
				"Hongdae",
				"2026-04-10",
				"host",
				Instant.parse("2026-04-14T03:00:00Z"),
				Instant.parse("2026-04-14T04:00:00Z"),
				"기록 본문",
				List.of("https://cdn.example.com/a.jpg")
			));

		mockMvc.perform(
			get("/api/meetings/55/logs/me")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.logId").value(101))
			.andExpect(jsonPath("$.meetingTitle").value("심야 이스케이프"))
			.andExpect(jsonPath("$.photos[0]").value("https://cdn.example.com/a.jpg"));
	}

	@Test
	void returnsMeetingLogDetailForAnotherUser() throws Exception {
		when(getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(101L, 9L)))
			.thenReturn(GetMeetingLogDetailUseCase.Result.of(
				101L,
				55L,
				"심야 이스케이프",
				"Deep Blue",
				"Hongdae",
				"2026-04-10",
				"host",
				Instant.parse("2026-04-14T03:00:00Z"),
				Instant.parse("2026-04-14T04:00:00Z"),
				"기록 본문",
				List.of("https://cdn.example.com/a.jpg")
			));

		mockMvc.perform(
			get("/api/logs/101")
				.principal(new UsernamePasswordAuthenticationToken(9L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.themeName").value("Deep Blue"))
			.andExpect(jsonPath("$.authorNickname").value("host"));
	}

	@Test
	void returnsUnauthorizedWhenCreateWithoutAuthentication() throws Exception {
		mockMvc.perform(
			post("/api/meetings/55/logs")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "body": "방탈출 기록입니다.",
					  "photos": []
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void returnsValidationErrorWhenBodyIsBlank() throws Exception {
		mockMvc.perform(
			post("/api/meetings/55/logs")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "body": "",
					  "photos": []
					}
					""")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON_VALIDATION_ERROR"));
	}

	@Test
	void returnsLogNotFoundWhenDetailDoesNotExist() throws Exception {
		when(getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(999L, 7L)))
			.thenThrow(new MeetingLogNotFoundException(999L));

		mockMvc.perform(
			get("/api/logs/999")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("LOG_NOT_FOUND"));
	}
}
