package com.bangpot.gallerylog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.bangpot.gallerylog.application.exception.MeetingLogNotFoundException;
import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.gallerylog.application.usecase.GetMyMeetingLogUseCase;

class GetMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	@Test
	void getsMyLogByMeeting() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"my log",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		));

		var detail = getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(meeting.getId(), 10L));

		assertThat(detail.meetingId()).isEqualTo(meeting.getId());
		assertThat(detail.authorNickname()).isEqualTo("host");
		assertThat(detail.photos()).containsExactly("https://cdn.example.com/a.jpg");
	}

	@Test
	void getsLogDetailForAuthor() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "detail log", List.of())
		);

		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(created.logId(), 10L));

		assertThat(detail.logId()).isEqualTo(created.logId());
		assertThat(detail.themeName()).isEqualTo("Deep Blue");
	}

	@Test
	void allowsLogDetailForAnotherUser() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "detail log", List.of())
		);

		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(created.logId(), 11L));

		assertThat(detail.logId()).isEqualTo(created.logId());
		assertThat(detail.authorNickname()).isEqualTo("host");
	}

	@Test
	void returnsNotFoundWhenMyLogDoesNotExist() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");

		assertThatThrownBy(() -> getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(
			meeting.getId(),
			10L
		))).isInstanceOf(MeetingLogNotFoundException.class);
	}
}
