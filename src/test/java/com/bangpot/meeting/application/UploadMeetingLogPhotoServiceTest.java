package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorage;
import com.bangpot.common.storage.StoredFile;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.service.UploadMeetingLogPhotoService;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

class UploadMeetingLogPhotoServiceTest {

	@Test
	void uploadsPhotoAndReturnsUrlAndSize() {
		FileStorage fileStorage = fileStorage("log-photos/stored-sample.jpg");
		var service = new UploadMeetingLogPhotoService(fileStorage, completedUserAccessService());
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());

		var result = service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, file));

		assertThat(result.sizeBytes()).isEqualTo(file.getSize());
		assertThat(result.url()).isEqualTo(
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/log-photos/stored-sample.jpg"
		);
		verify(fileStorage).store("log-photos", file);
	}

	@Test
	void rejectsUnsupportedExtension() {
		var service = service();
		var file = new MockMultipartFile("file", "sample.gif", "image/gif", "image-bytes".getBytes());

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file", "사진은 jpg, jpeg, png 형식만 허용합니다."));
	}

	@Test
	void rejectsUnsupportedContentType() {
		var service = service();
		var file = new MockMultipartFile("file", "sample.jpg", "text/plain", "image-bytes".getBytes());

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file.contentType", "사진은 jpg, jpeg, png 형식만 허용합니다."));
	}

	@Test
	void rejectsFileLargerThanFiveMegabytes() {
		var service = service();
		byte[] bytes = new byte[(int) (5L * 1024 * 1024) + 1];
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", bytes);

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file.sizeBytes", "사진은 5MB를 초과할 수 없습니다."));
	}

	private UploadMeetingLogPhotoService service() {
		return new UploadMeetingLogPhotoService(
			fileStorage("log-photos/service-sample.jpg"),
			completedUserAccessService()
		);
	}

	private CompletedUserAccessService completedUserAccessService() {
		return mock(CompletedUserAccessService.class);
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
