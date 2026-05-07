package com.bangpot.meeting.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bangpot.common.error.ApiErrorResponseFactory;
import com.bangpot.common.error.GlobalApiExceptionHandler;
import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.meeting.domain.view.MeetingLogDetailView;
import com.bangpot.meeting.domain.view.MyMeetingLogView;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MeetingLogController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class MeetingLogControllerTest {

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

	@MockitoBean
	private UploadMeetingLogPhotoUseCase uploadMeetingLogPhotoUseCase;

	@Test
	void createsMeetingLogForAuthenticatedUser() throws Exception {
		when(createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			55L,
			7L,
			"log body",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		))).thenReturn(CreateMeetingLogUseCase.Result.of(101L, 55L));

		mockMvc.perform(
			post("/api/meetings/55/logs")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "body": "log body",
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
			"updated body",
			List.of(UpdateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/b.png", 2048L))
		))).thenReturn(UpdateMeetingLogUseCase.Result.of(101L, 55L));

		mockMvc.perform(
			patch("/api/logs/101")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "body": "updated body",
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
		when(deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(5L, 101L, 7L, null)))
			.thenReturn(DeleteMeetingLogUseCase.Result.of(101L, DeleteMeetingLogUseCase.DeletedBy.AUTHOR));

		mockMvc.perform(
			delete("/api/crews/5/logs/101")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.logId").value(101))
			.andExpect(jsonPath("$.deletedBy").value("AUTHOR"));
	}

	@Test
	void deletesMeetingLogByLeaderWithReason() throws Exception {
		when(deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(5L, 101L, 7L, "policy violation")))
			.thenReturn(DeleteMeetingLogUseCase.Result.of(101L, DeleteMeetingLogUseCase.DeletedBy.LEADER));

		mockMvc.perform(
			delete("/api/crews/5/logs/101")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "deleteReason": "policy violation"
					}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.logId").value(101))
			.andExpect(jsonPath("$.deletedBy").value("LEADER"));
	}

	@Test
	void returnsMyMeetingLogDetail() throws Exception {
		when(getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(55L, 7L)))
			.thenReturn(MyMeetingLogView.of(
				101L,
				55L,
				"Friday Escape",
				"Deep Blue",
				"Hongdae",
				"2026-04-10",
				"host",
				Instant.parse("2026-04-14T03:00:00Z"),
				Instant.parse("2026-04-14T04:00:00Z"),
				"log body",
				List.of("https://cdn.example.com/a.jpg")
			));

		mockMvc.perform(
			get("/api/meetings/55/logs/me")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("EXISTS"))
			.andExpect(jsonPath("$.logId").value(101))
			.andExpect(jsonPath("$.meetingTitle").value("Friday Escape"))
			.andExpect(jsonPath("$.photos[0]").value("https://cdn.example.com/a.jpg"));
	}

	@Test
	void returnsNotWrittenStatusWhenMyLogDoesNotExist() throws Exception {
		when(getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(55L, 7L)))
			.thenReturn(MyMeetingLogView.notWritten());

		mockMvc.perform(
			get("/api/meetings/55/logs/me")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("NOT_WRITTEN"))
			.andExpect(jsonPath("$.logId").isEmpty())
			.andExpect(jsonPath("$.body").isEmpty());
	}

	@Test
	void returnsDeletedBlockedStatusWhenMyLogWasDeleted() throws Exception {
		when(getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(55L, 7L)))
			.thenReturn(MyMeetingLogView.deletedBlocked());

		mockMvc.perform(
			get("/api/meetings/55/logs/me")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DELETED_BLOCKED"))
			.andExpect(jsonPath("$.logId").isEmpty())
			.andExpect(jsonPath("$.body").isEmpty());
	}

	@Test
	void returnsMeetingLogDetailForAnotherUser() throws Exception {
		when(getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(5L, 101L, 9L)))
			.thenReturn(MeetingLogDetailView.of(
				101L,
				55L,
				"Friday Escape",
				"Deep Blue",
				"Hongdae",
				"2026-04-10",
				"host",
				Instant.parse("2026-04-14T03:00:00Z"),
				Instant.parse("2026-04-14T04:00:00Z"),
				"log body",
				List.of("https://cdn.example.com/a.jpg")
			));

		mockMvc.perform(
			get("/api/crews/5/logs/101")
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
					  "body": "log body",
					  "photos": []
					}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	@Test
	void uploadsLogPhotoForAuthenticatedUser() throws Exception {
		var file = new MockMultipartFile("file", "sample.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

		when(uploadMeetingLogPhotoUseCase.handle(org.mockito.ArgumentMatchers.any()))
			.thenReturn(UploadMeetingLogPhotoUseCase.Result.of(
				"https://banglog-image.s3.ap-northeast-2.amazonaws.com/log-photos/stored-sample.jpg",
				(Long) file.getSize()
			));

		mockMvc.perform(
			multipart("/api/meetings/55/logs/photos")
				.file(file)
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.url").value(
				"https://banglog-image.s3.ap-northeast-2.amazonaws.com/log-photos/stored-sample.jpg"
			))
			.andExpect(jsonPath("$.sizeBytes").value((int) file.getSize()));
	}

	@Test
	void returnsUnauthorizedWhenUploadWithoutAuthentication() throws Exception {
		var file = new MockMultipartFile("file", "sample.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

		mockMvc.perform(
			multipart("/api/meetings/55/logs/photos")
				.file(file)
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
		when(getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(5L, 999L, 7L)))
			.thenThrow(new MeetingLogNotFoundException(999L));

		mockMvc.perform(
			get("/api/crews/5/logs/999")
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("LOG_NOT_FOUND"));
	}
}
