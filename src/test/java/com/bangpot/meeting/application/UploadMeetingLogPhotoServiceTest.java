package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorageProperties;
import com.bangpot.common.storage.LocalFileStorage;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.service.UploadMeetingLogPhotoService;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

class UploadMeetingLogPhotoServiceTest {

	@TempDir
	Path tempDir;

	@Test
	void uploadsPhotoAndReturnsUrlAndSize() throws Exception {
		var service = new UploadMeetingLogPhotoService(fileStorage(), completedUserAccessService());
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());

		var result = service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, "http://localhost:8080", file));

		assertThat(result.sizeBytes()).isEqualTo(file.getSize());
		assertThat(result.url()).startsWith("http://localhost:8080/uploads/log-photos/");
		assertThat(Files.list(tempDir.resolve("log-photos")).count()).isEqualTo(1);
	}

	@Test
	void rejectsUnsupportedExtension() {
		var service = new UploadMeetingLogPhotoService(fileStorage(), completedUserAccessService());
		var file = new MockMultipartFile("file", "sample.gif", "image/gif", "image-bytes".getBytes());

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, "http://localhost:8080", file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file", "사진은 jpg, jpeg, png 형식만 허용됩니다."));
	}

	@Test
	void rejectsFileLargerThanFiveMegabytes() {
		var service = new UploadMeetingLogPhotoService(fileStorage(), completedUserAccessService());
		byte[] bytes = new byte[(int) (5L * 1024 * 1024) + 1];
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", bytes);

		assertThatThrownBy(() -> service.handle(UploadMeetingLogPhotoUseCase.Command.of(7L, "http://localhost:8080", file)))
			.isInstanceOf(MeetingLogRequestValidationException.class)
			.extracting(ex -> ((MeetingLogRequestValidationException) ex).getFieldErrors())
			.asList()
			.contains(new ApiErrorField("file.sizeBytes", "사진은 5MB를 초과할 수 없습니다."));
	}

	private CompletedUserAccessService completedUserAccessService() {
		return mock(CompletedUserAccessService.class);
	}

	private LocalFileStorage fileStorage() {
		var properties = new FileStorageProperties();
		properties.setRootDirectory(tempDir.toString());
		return new LocalFileStorage(properties);
	}
}
