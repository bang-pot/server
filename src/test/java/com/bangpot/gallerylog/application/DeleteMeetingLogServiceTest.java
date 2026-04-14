package com.bangpot.gallerylog.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.gallerylog.application.exception.MeetingLogNotFoundException;
import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.DeleteMeetingLogUseCase;

class DeleteMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	@Test
	void deletesOwnLog() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"삭제할 기록",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		));

		deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(created.logId(), 10L));

		assertThatThrownBy(() -> getMeetingLogDetailUseCase.handle(
			com.bangpot.gallerylog.application.usecase.GetMeetingLogDetailUseCase.Query.of(created.logId(), 10L)
		)).isInstanceOf(MeetingLogNotFoundException.class);
	}

	@Test
	void rejectsDeleteByAnotherUser() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "본문", List.of()));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			created.logId(),
			11L
		))).isInstanceOf(AccessDeniedException.class);
	}
}
