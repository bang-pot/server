package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;

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
			GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 10L)
		)).isInstanceOf(MeetingLogNotFoundException.class);
	}

	@Test
	void rejectsDeleteByAnotherUser() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"삭제 방지",
			List.of()
		));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			created.logId(),
			11L
		))).isInstanceOf(AccessDeniedException.class);
	}
}

