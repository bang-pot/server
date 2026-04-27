package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.domain.view.MyMeetingLogView;

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

		assertThat(detail.status()).isEqualTo(MyMeetingLogView.Status.EXISTS);
		assertThat(detail.meetingId()).isEqualTo(meeting.getId());
		assertThat(detail.authorNickname()).isEqualTo("host");
		assertThat(detail.photos()).containsExactly("https://cdn.example.com/a.jpg");
	}

	@Test
	void getsLogDetailForAuthorWithinCrewContext() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "detail log", List.of())
		);

		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 10L));

		assertThat(detail.logId()).isEqualTo(created.logId());
		assertThat(detail.themeName()).isEqualTo("Deep Blue");
	}

	@Test
	void allowsLogDetailForAnotherCrewMember() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		activeCrewMember(1L, 11L);
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "detail log", List.of())
		);

		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 11L));

		assertThat(detail.logId()).isEqualTo(created.logId());
		assertThat(detail.authorNickname()).isEqualTo("host");
	}

	@Test
	void deniesLogDetailForUserOutsideCrew() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "detail log", List.of())
		);

		assertThatThrownBy(() -> getMeetingLogDetailUseCase.handle(
			GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 11L)
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void returnsNotWrittenWhenMyLogDoesNotExist() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");

		var result = getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(meeting.getId(), 10L));

		assertThat(result.status()).isEqualTo(MyMeetingLogView.Status.NOT_WRITTEN);
		assertThat(result.logId()).isNull();
		assertThat(result.body()).isNull();
		assertThat(result.photos()).isEmpty();
	}

	@Test
	void returnsDeletedBlockedWhenSoftDeletedLogExists() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"my log",
			List.of()
		));
		deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(1L, created.logId(), 10L, null));

		var result = getMyMeetingLogUseCase.handle(GetMyMeetingLogUseCase.Query.of(meeting.getId(), 10L));

		assertThat(result.status()).isEqualTo(MyMeetingLogView.Status.DELETED_BLOCKED);
		assertThat(result.logId()).isNull();
		assertThat(result.body()).isNull();
		assertThat(result.photos()).isEmpty();
	}
}
