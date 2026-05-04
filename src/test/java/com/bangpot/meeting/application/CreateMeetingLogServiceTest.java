package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.application.exception.MeetingLogAlreadyExistsException;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.exception.MeetingLogWriteNotAllowedException;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.service.CreateMeetingLogService;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

class CreateMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	@Test
	void createsLogForCompletedHost() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");

		var result = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"정말 재미있었던 방탈출이었어요.",
			java.util.List.of(
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L)
			)
		));

		assertThat(result.logId()).isNotNull();
		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(meetingLogRepository.findById(result.logId()))
			.get()
			.extracting(MeetingLog::getCreatedAt, MeetingLog::getUpdatedAt)
			.containsExactly(NOW, NOW);
	}

	@Test
	void allowsCompletedJoinedParticipantToCreateLog() {
		completedUser(10L, "host");
		completedUser(11L, "member");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		participant(meeting.getId(), 11L, MeetingParticipationStatus.JOINED);

		var result = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			11L,
			"같이 참여했던 멤버와 기록을 남기고 싶었습니다.",
			java.util.List.of()
		));

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
	}

	@Test
	void rejectsCompletedLeftParticipantFromCreatingLog() {
		completedUser(10L, "host");
		completedUser(11L, "member");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		participant(meeting.getId(), 11L, MeetingParticipationStatus.LEFT);

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			11L,
			"LEFT 이력은 작성 권한이 없습니다.",
			java.util.List.of()
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsWhenMeetingIsNotCompleted() {
		completedUser(10L, "host");
		var meeting = recruitingMeeting(1L, 10L, "Deep Blue");

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"완료 전에 작성할 수 없습니다.",
			java.util.List.of()
		))).isInstanceOf(MeetingLogWriteNotAllowedException.class);
	}

	@Test
	void rejectsWhenUserHasNoParticipationHistory() {
		completedUser(10L, "host");
		completedUser(99L, "outsider");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			99L,
			"무관한 사용자는 작성할 수 없습니다.",
			java.util.List.of()
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsDuplicateLogPerMeetingAndAuthor() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"첫 기록",
			java.util.List.of()
		));

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"두 번째 기록",
			java.util.List.of()
		))).isInstanceOf(MeetingLogAlreadyExistsException.class);
	}

	@Test
	void translatesDuplicateInsertRaceToDomainException() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		createMeetingLogUseCase = new CreateMeetingLogService(
			completedUserAccessService,
			meetingRepository,
			meetingParticipantRepository,
			new DuplicateInsertMeetingLogRepository(meetingLogRepository),
			meetingLogPhotoRepository,
			Clock.fixed(NOW, ZoneOffset.UTC)
		);

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"동시에 작성된 방탈로그입니다.",
			java.util.List.of()
		))).isInstanceOf(MeetingLogAlreadyExistsException.class);
	}

	@Test
	void validatesPhotoExtensionAndSizeAndCount() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"사진 검증 실패",
			java.util.List.of(
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.gif", 1024L),
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/b.jpg", 6L * 1024 * 1024),
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/c.jpg", 1024L),
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/d.jpg", 1024L),
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/e.jpg", 1024L),
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/f.jpg", 1024L)
			)
		))).isInstanceOf(MeetingLogRequestValidationException.class);
	}

	private record DuplicateInsertMeetingLogRepository(MeetingLogRepository delegate) implements MeetingLogRepository {

		@Override
		public MeetingLog save(MeetingLog log) {
			throw new DataIntegrityViolationException("uk_meeting_logs_meeting_author");
		}

		@Override
		public java.util.Optional<MeetingLog> findById(Long logId) {
			return delegate.findById(logId);
		}

		@Override
		public java.util.Optional<MeetingLog> findByIdForUpdate(Long logId) {
			return delegate.findByIdForUpdate(logId);
		}

		@Override
		public java.util.Optional<MeetingLog> findActiveLogInCrewForUpdate(Long crewId, Long logId) {
			return delegate.findActiveLogInCrewForUpdate(crewId, logId);
		}

		@Override
		public java.util.Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return delegate.findByMeetingIdAndAuthorUserId(meetingId, authorUserId);
		}

		@Override
		public boolean existsAnyByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return false;
		}

		@Override
		public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return delegate.existsDeletedByMeetingIdAndAuthorUserId(meetingId, authorUserId);
		}
	}
}
