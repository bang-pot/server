package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.application.exception.MeetingLogAlreadyExistsException;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.exception.MeetingLogWriteNotAllowedException;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;

class CreateMeetingLogServiceTest extends AbstractMeetingLogServicesTest {

	@Test
	void createsLogForCompletedHost() {
		completedUser(10L, "host");
		var meeting = completedMeeting(1L, 10L, "Deep Blue");

		var result = createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"?�말 ?��??�었??방탈출이?�어??",
			java.util.List.of(
				CreateMeetingLogUseCase.PhotoInput.of("https://cdn.example.com/a.jpg", 1024L)
			)
		));

		assertThat(result.logId()).isNotNull();
		assertThat(result.meetingId()).isEqualTo(meeting.getId());
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
			"같이 참여?�던 멤버??기록???�길 ???�어???�니??",
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
			"LEFT ?�력?� ?�성 권한???�습?�다.",
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
			"?�료 ?�에 ?�성???�는 ?�습?�다.",
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
			"무�????�용?�는 ?�성?????�습?�다.",
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
			"�?기록",
			java.util.List.of()
		));

		assertThatThrownBy(() -> createMeetingLogUseCase.handle(CreateMeetingLogUseCase.Command.of(
			meeting.getId(),
			10L,
			"??번째 기록",
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
			"?�진 검�??�패",
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
}

