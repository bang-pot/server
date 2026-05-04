package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.service.UpdateMeetingLogService;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;

class UpdateMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	private static final Instant UPDATED_AT = NOW.plusSeconds(300);

	@Test
	void updatesOwnLogAndReplacesPhotos() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"원래 작성한 후기입니다.",
			List.of(CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L))
		));

		var result = updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"수정한 후기입니다.",
			List.of(
				UpdateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/b.png", 2048L),
				UpdateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/c.jpeg", 2048L)
			)
		));

		assertThat(result.logId()).isEqualTo(created.logId());
		var detail = getMeetingLogDetailUseCase.handle(GetMeetingLogDetailUseCase.Query.of(1L, created.logId(), 10L));
		assertThat(detail.body()).isEqualTo("수정한 후기입니다.");
		assertThat(detail.photos()).containsExactly(
			"https://cdn.example.com/b.png",
			"https://cdn.example.com/c.jpeg"
		);
		assertThat(meetingLogRepository.findById(created.logId()))
			.get()
			.satisfies(log -> {
				assertThat(log.getCreatedAt()).isEqualTo(NOW);
				assertThat(log.getUpdatedAt()).isEqualTo(NOW);
			});
	}

	@Test
	void updatesTimestampWithInjectedClock() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"원래 작성한 후기입니다.",
			List.of()
		));
		updateMeetingLogUseCase = new UpdateMeetingLogService(
			completedUserAccessService,
			meetingLogRepository,
			meetingLogPhotoRepository,
			Clock.fixed(UPDATED_AT, ZoneOffset.UTC)
		);

		updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"수정한 후기입니다.",
			List.of()
		));

		assertThat(meetingLogRepository.findById(created.logId()))
			.get()
			.satisfies(log -> {
				assertThat(log.getCreatedAt()).isEqualTo(NOW);
				assertThat(log.getUpdatedAt()).isEqualTo(UPDATED_AT);
			});
	}

	@Test
	void locksLogWhenUpdating() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"원래 작성한 후기입니다.",
			List.of()
		));
		meetingLogRepository.resetLockTracking();

		updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			10L,
			"수정한 후기입니다.",
			List.of()
		));

		assertThat(meetingLogRepository.findByIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsUpdateByAnotherUser() {
		completedUser(10L, "host");
		completedUser(11L, "other");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");
		var created = createMeetingLogUseCase.handle(
			CreateMeetingLogUseCase.Command.of(meeting.getId(), 10L, "후기입니다.", List.of())
		);

		assertThatThrownBy(() -> updateMeetingLogUseCase.handle(UpdateMeetingLogUseCase.Command.of(
			created.logId(),
			11L,
			"다른 내용으로 수정 시도",
			List.of()
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
