package com.banglog.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.exception.MeetingLogNotFoundException;
import com.banglog.meeting.application.exception.MeetingLogRequestValidationException;
import com.banglog.meeting.application.service.DeleteMeetingLogService;
import com.banglog.meeting.application.usecase.CreateMeetingLogUseCase;
import com.banglog.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.banglog.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.banglog.meeting.domain.MeetingParticipationStatus;

class DeleteMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	private static final Instant DELETED_AT = NOW.plusSeconds(600);

	@Test
	void authorDeletesOwnLogWithoutReason() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"delete target",
			List.of(CreateMeetingLogUseCase.PhotoInput.of(1L)),
			"SUCCESS"
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
			List.of(),
			"SUCCESS"
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
			List.of(),
			"SUCCESS"
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
			List.of(),
			"SUCCESS"
		));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			1L,
			created.logId(),
			12L,
			null
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsDeleteWhenLogDoesNotBelongToCrew() {
		completedUser(10L, "host");
		var sourceMeeting = completedMeeting(1L, 10L, "Deep Blue");
		completedMeeting(2L, 10L, "Red Planet");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			sourceMeeting.getId(),
			10L,
			"1번 크루의 방탈로그입니다.",
			List.of(),
			"SUCCESS"
		));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			2L,
			created.logId(),
			10L,
			null
		))).isInstanceOf(MeetingLogNotFoundException.class);
	}

	@Test
	void locksLogWhenDeleting() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"삭제할 방탈로그입니다.",
			List.of(),
			"SUCCESS"
		));
		meetingLogRepository.resetLockTracking();

		deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(1L, created.logId(), 10L, null));

		assertThat(meetingLogRepository.findByIdForUpdateCalled()).isTrue();
	}

	@Test
	void deletesTimestampWithInjectedClock() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"삭제할 방탈로그입니다.",
			List.of(),
			"SUCCESS"
		));
		deleteMeetingLogUseCase = new DeleteMeetingLogService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingLogRepository,
			Clock.fixed(DELETED_AT, ZoneOffset.UTC)
		);

		deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(1L, created.logId(), 10L, null));

		assertThat(meetingLogRepository.findAnyById(created.logId()))
			.get()
			.satisfies(log -> {
				assertThat(log.getDeletedAt()).isEqualTo(DELETED_AT);
				assertThat(log.getUpdatedAt()).isEqualTo(DELETED_AT);
			});
	}

	@Test
	void rejectsDeleteByNonMemberWithKoreanMessage() {
		completedUser(10L, "host");
		completedUser(11L, "outsider");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"삭제할 방탈로그입니다.",
			List.of(),
			"SUCCESS"
		));

		assertThatThrownBy(() -> deleteMeetingLogUseCase.handle(DeleteMeetingLogUseCase.Command.of(
			1L,
			created.logId(),
			11L,
			null
		))).isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("활성 크루 멤버만 방탈로그를 삭제할 수 있습니다.");
	}

	@Test
	void deletesLogInTransaction() throws NoSuchMethodException {
		assertThat(DeleteMeetingLogService.class
			.getMethod("handle", DeleteMeetingLogUseCase.Command.class))
			.satisfies(method -> assertThat(method.isAnnotationPresent(Transactional.class)).isTrue());
	}
}
