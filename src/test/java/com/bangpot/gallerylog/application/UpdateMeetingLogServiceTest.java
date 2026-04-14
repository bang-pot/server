package com.bangpot.gallerylog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.gallerylog.application.usecase.UpdateMeetingLogUseCase;

class UpdateMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	@Test
	void updatesOwnLogAndReplacesPhotos() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"처음 본문",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		));

		var result = updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"수정된 본문",
			List.of(
				UpdateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/b.png", 2048L),
				UpdateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/c.jpeg", 2048L)
			)
		));

		assertThat(result.logId()).isEqualTo(created.logId());
		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(created.logId(), 10L));
		assertThat(detail.body()).isEqualTo("수정된 본문");
		assertThat(detail.photos()).containsExactly(
			"https://cdn.example.com/b.png",
			"https://cdn.example.com/c.jpeg"
		);
	}

	@Test
	void rejectsUpdateByAnotherUser() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "본문", List.of()));

		assertThatThrownBy(() -> updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			11L,
			"남의 글 수정",
			List.of()
		))).isInstanceOf(AccessDeniedException.class);
	}
}
