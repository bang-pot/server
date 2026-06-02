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

import com.banglog.meeting.application.service.UpdateMeetingLogService;
import com.banglog.meeting.application.usecase.CreateMeetingLogUseCase;
import com.banglog.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.banglog.meeting.application.usecase.UpdateMeetingLogUseCase;
import com.banglog.meeting.domain.MeetingResult;

class UpdateMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	private static final Instant UPDATED_AT = NOW.plusSeconds(300);

	@Test
	void updatesOwnLogAndReplacesPhotosAndResult() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"Original log.",
			List.of(CreateMeetingLogUseCase.PhotoInput.of(1L)),
			"SUCCESS"
		));

		var result = updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"Updated log.",
			List.of(
				UpdateMeetingLogUseCase.PhotoInput.of(2L),
				UpdateMeetingLogUseCase.PhotoInput.of(3L)
			),
			"FAILURE"
		));

		assertThat(result.logId()).isEqualTo(created.logId());
		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 10L));
		assertThat(detail.body()).isEqualTo("Updated log.");
		assertThat(detail.result()).isEqualTo(MeetingResult.FAILURE);
		assertThat(detail.photos()).containsExactly(
			"https://cdn.example.com/upload-2.jpg",
			"https://cdn.example.com/upload-3.jpg"
		);
		assertThat(meetingLogRepository.findById(created.logId()))
			.get()
			.satisfies(log -> {
				assertThat(log.getCreatedAt()).isEqualTo(NOW);
				assertThat(log.getUpdatedAt()).isEqualTo(NOW);
				assertThat(log.getResult()).isEqualTo(MeetingResult.FAILURE);
			});
	}

	@Test
	void updatesTimestampWithInjectedClock() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"Original log.",
			List.of(),
			"SUCCESS"
		));
		updateMeetingLogUseCase = new UpdateMeetingLogService(
			completedUserAccessService,
			meetingLogRepository,
			meetingLogPhotoRepository,
			attachImageUploadUseCase,
			Clock.fixed(UPDATED_AT, ZoneOffset.UTC)
		);

		updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"Updated log.",
			List.of(),
			"SUCCESS"
		));

		assertThat(meetingLogRepository.findById(created.logId()))
			.get()
			.satisfies(log -> {
				assertThat(log.getCreatedAt()).isEqualTo(NOW);
				assertThat(log.getUpdatedAt()).isEqualTo(UPDATED_AT);
			});
	}

	@Test
	void rejectsUpdateWithoutResult() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"Original log.",
			List.of(),
			"SUCCESS"
		));

		assertThatThrownBy(() -> updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"Updated log.",
			List.of()
		))).isInstanceOf(com.banglog.meeting.application.exception.MeetingLogRequestValidationException.class);
	}

	@Test
	void locksLogWhenUpdating() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"Original log.",
			List.of(),
			"SUCCESS"
		));
		meetingLogRepository.resetLockTracking();

		updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"Updated log.",
			List.of(),
			"SUCCESS"
		));

		assertThat(meetingLogRepository.findByIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsUpdateByAnotherUser() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "Original log.", List.of(), "SUCCESS")
		);

		assertThatThrownBy(() -> updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			11L,
			"Updated by another user.",
			List.of(),
			"FAILURE"
		))).isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("작성자 본인만 방탈로그를 수정할 수 있습니다.");
	}

	@Test
	void updatesLogInTransaction() throws NoSuchMethodException {
		assertThat(UpdateMeetingLogService.class
			.getMethod("handle", UpdateMeetingLogUseCase.Command.class))
			.satisfies(method -> assertThat(method.isAnnotationPresent(Transactional.class)).isTrue());
	}
}
