package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.mock.web.MockMultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorage;
import com.bangpot.common.storage.StoredFile;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.UploadMeetingLogPhotoService;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.user.application.service.CompletedUserAccessService;

class UploadMeetingLogPhotoServiceTest {

	@Test
	void uploadsPhotoAndReturnsUrlAndSize() {
		var meetingRepository = meetingRepository(completedMeeting(1L, 7L));
		FileStorage fileStorage = fileStorage("log-photos/stored-sample.jpg");
		var service = new UploadMeetingLogPhotoService(
			fileStorage,
			completedUserAccessService(),
			meetingRepository,
			mock(MeetingParticipantRepository.class)
		);
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());

		var result = service.handle(UploadMeetingLogPhotoUseCase.Command.of(1L, 7L, file));

		assertThat(result.sizeBytes()).isEqualTo(file.getSize());
		assertThat(result.url()).isEqualTo(
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/log-photos/stored-sample.jpg"
		);
		verify(fileStorage).store("log-photos", file);
	}

	@Test
	void rejectsUserWithoutParticipantHistoryAndDoesNotStoreFile() {
		FileStorage fileStorage = mock(FileStorage.class);
		var meeting = completedMeeting(1L, 99L);
		var meetingParticipantRepository = mock(MeetingParticipantRepository.class);
		when(meetingParticipantRepository.findByMeetingIdAndUserId(1L, 7L)).thenReturn(Optional.empty());
		var service = new UploadMeetingLogPhotoService(
			fileStorage,
			completedUserAccessService(),
			meetingRepository(meeting),
			meetingParticipantRepository
		);
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(1L, 7L, file)))
			.isInstanceOf(AccessDeniedException.class);
		verify(fileStorage, never()).store(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
	}

	@Test
	void uploadsPhotoForParticipantWithHistory() {
		var meeting = completedMeeting(1L, 99L);
		var meetingParticipantRepository = mock(MeetingParticipantRepository.class);
		when(meetingParticipantRepository.findByMeetingIdAndUserId(1L, 7L))
			.thenReturn(Optional.of(MeetingParticipant.join(1L, 7L)));
		FileStorage fileStorage = fileStorage("log-photos/participant-sample.jpg");
		var service = new UploadMeetingLogPhotoService(
			fileStorage,
			completedUserAccessService(),
			meetingRepository(meeting),
			meetingParticipantRepository
		);
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());

		var result = service.handle(UploadMeetingLogPhotoUseCase.Command.of(1L, 7L, file));

		assertThat(result.url()).isEqualTo(
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/log-photos/participant-sample.jpg"
		);
	}

	@Test
	void rejectsUnsupportedExtension() {
		var service = serviceFor(completedMeeting(1L, 7L));
		var file = new MockMultipartFile("file", "sample.gif", "image/gif", "image-bytes".getBytes());

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(1L, 7L, file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file", "사진은 jpg, jpeg, png 형식만 허용됩니다."));
	}

	@Test
	void rejectsUnsupportedContentType() {
		var service = serviceFor(completedMeeting(1L, 7L));
		var file = new MockMultipartFile("file", "sample.jpg", "text/plain", "image-bytes".getBytes());

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(1L, 7L, file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file.contentType", "사진은 jpg, jpeg, png 형식만 허용됩니다."));
	}

	@Test
	void rejectsFileLargerThanFiveMegabytes() {
		var service = serviceFor(completedMeeting(1L, 7L));
		byte[] bytes = new byte[(int) (5L * 1024 * 1024) + 1];
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", bytes);

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(1L, 7L, file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file.sizeBytes", "사진은 5MB를 초과할 수 없습니다."));
	}

	private CompletedUserAccessService completedUserAccessService() {
		return mock(CompletedUserAccessService.class);
	}

	private UploadMeetingLogPhotoService serviceFor(Meeting meeting) {
		return new UploadMeetingLogPhotoService(
			fileStorage("log-photos/service-sample.jpg"),
			completedUserAccessService(),
			meetingRepository(meeting),
			mock(MeetingParticipantRepository.class)
		);
	}

	private MeetingRepository meetingRepository(Meeting meeting) {
		MeetingRepository meetingRepository = mock(MeetingRepository.class);
		when(meetingRepository.findById(meeting.getId())).thenReturn(Optional.of(meeting));
		return meetingRepository;
	}

	private Meeting completedMeeting(Long meetingId, Long hostUserId) {
		Meeting meeting = Meeting.create(
			1L,
			hostUserId,
			"방탈출",
			"테마",
			"강남",
			"2026-05-05",
			"12:00",
			4,
			100000,
			null,
			null
		);
		meeting.assignId(meetingId);
		meeting.closeRecruitment();
		meeting.complete();
		return meeting;
	}

	private FileStorage fileStorage(String key) {
		FileStorage fileStorage = mock(FileStorage.class);
		when(fileStorage.store(org.mockito.ArgumentMatchers.eq("log-photos"), org.mockito.ArgumentMatchers.any()))
			.thenReturn(new StoredFile(
				key,
				"https://banglog-image.s3.ap-northeast-2.amazonaws.com/" + key,
				"image-bytes".getBytes().length,
				key.substring(key.lastIndexOf('/') + 1)
			));
		return fileStorage;
	}
}
