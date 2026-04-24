package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

class DeleteMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	@Test
	void authorDeletesOwnLogWithoutReason() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"delete target",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		));

		var result = deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(1L, created.logId(), 10L, null));

		assertThat(result.logId()).isEqualTo(created.logId());
		assertThat(result.deletedBy()).isEqualTo(DeleteMeetingLogUseCase.DeletedBy.AUTHOR);
		assertThatThrownBy(() -> getMeetingLogDetailUseCase.handle(
			GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 10L)
		)).isInstanceOf(MeetingLogNotFoundException.class);
	}

	@Test
	void leaderDeletesOtherMembersLogWithReason() {
		completedUser(10L, "leader");
		completedUser(11L, "member");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		activeCrewMember(1L, 11L);
		participant(meeting.getId(), 11L, MeetingParticipationStatus.JOINED);
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			11L,
			"member log",
			List.of()
		));

		var result = deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			1L,
			created.logId(),
			10L,
			"운영 정책에 맞지 않는 기록"
		));

		assertThat(result.logId()).isEqualTo(created.logId());
		assertThat(result.deletedBy()).isEqualTo(DeleteMeetingLogUseCase.DeletedBy.LEADER);
		assertThatThrownBy(() -> getMeetingLogDetailUseCase.handle(
			GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 10L)
		)).isInstanceOf(MeetingLogNotFoundException.class);
	}

	@Test
	void leaderDeleteRequiresReasonWhenDeletingOthersLog() {
		completedUser(10L, "leader");
		completedUser(11L, "member");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		activeCrewMember(1L, 11L);
		participant(meeting.getId(), 11L, MeetingParticipationStatus.JOINED);
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			11L,
			"member log",
			List.of()
		));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			1L,
			created.logId(),
			10L,
			" "
		))).isInstanceOf(MeetingLogRequestValidationException.class);
	}

	@Test
	void rejectsDeleteByRegularCrewMemberForAnotherUsersLog() {
		completedUser(10L, "leader");
		completedUser(11L, "author");
		completedUser(12L, "member");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		activeCrewMember(1L, 11L);
		activeCrewMember(1L, 12L);
		participant(meeting.getId(), 11L, MeetingParticipationStatus.JOINED);
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			11L,
			"member log",
			List.of()
		));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			1L,
			created.logId(),
			12L,
			null
		))).isInstanceOf(AccessDeniedException.class);
	}
}
