package com.banglog.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.common.storage.FileStorage;
import com.banglog.image.application.policy.MeetingLogPhotoUploadPolicy;
import com.banglog.image.application.port.ImageUploadRepository;
import com.banglog.image.application.service.TemporaryImageUploadService;
import com.banglog.image.application.usecase.UploadTemporaryImageUseCase;
import com.banglog.image.domain.ImageUpload;
import com.banglog.image.domain.ImageUploadCategory;
import com.banglog.image.domain.ImageUploadStatus;
import com.banglog.user.application.service.CompletedUserAccessService;

class TemporaryImageUploadServiceTest {

	private static final Instant NOW = Instant.parse("2026-05-17T12:00:00Z");

	@Test
	void uploadsTemporaryImageInTransaction() throws NoSuchMethodException {
		assertThat(TemporaryImageUploadService.class
			.getMethod("handle", UploadTemporaryImageUseCase.Command.class))
			.satisfies(method -> assertThat(method.isAnnotationPresent(Transactional.class)).isTrue());
	}

	@Test
	void uploadsTemporaryImageAfterCompletedUserValidation() {
		InMemoryImageUploadRepository imageUploadRepository = new InMemoryImageUploadRepository();
		FileStorage fileStorage = mock(FileStorage.class);
		CompletedUserAccessService completedUserAccessService = mock(CompletedUserAccessService.class);
		MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());
		when(fileStorage.store("temp/log-photos", file)).thenReturn("temp/log-photos/stored.jpg");
		when(fileStorage.publicUrl("temp/log-photos/stored.jpg"))
			.thenReturn("https://cdn.example.com/temp/log-photos/stored.jpg");
		TemporaryImageUploadService service = service(completedUserAccessService, imageUploadRepository, fileStorage);

		UploadTemporaryImageUseCase.Result result = service.handle(UploadTemporaryImageUseCase.Command.of(
			10L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			file
		));

		assertThat(result.uploadId()).isEqualTo(1L);
		assertThat(result.url()).isEqualTo("https://cdn.example.com/temp/log-photos/stored.jpg");
		assertThat(result.sizeBytes()).isEqualTo(file.getSize());
		verify(completedUserAccessService).validateCompletedUser(
			10L,
			"완료된 사용자만 이미지를 업로드할 수 있습니다."
		);
		ImageUpload upload = imageUploadRepository.findByIdForUpdate(1L).orElseThrow();
		assertThat(upload.getUploaderUserId()).isEqualTo(10L);
		assertThat(upload.getCategory()).isEqualTo(ImageUploadCategory.MEETING_LOG_PHOTO);
		assertThat(upload.getStatus()).isEqualTo(ImageUploadStatus.TEMP);
		assertThat(upload.getTempKey()).isEqualTo("temp/log-photos/stored.jpg");
		assertThat(upload.getExpiresAt()).isEqualTo(NOW.plusSeconds(24 * 60 * 60));
	}

	private TemporaryImageUploadService service(
		CompletedUserAccessService completedUserAccessService,
		ImageUploadRepository imageUploadRepository,
		FileStorage fileStorage
	) {
		return new TemporaryImageUploadService(
			completedUserAccessService,
			imageUploadRepository,
			fileStorage,
			List.of(new MeetingLogPhotoUploadPolicy()),
			Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	private static class InMemoryImageUploadRepository implements ImageUploadRepository {

		private final Map<Long, ImageUpload> uploads = new HashMap<>();
		private long sequence = 1L;

		@Override
		public ImageUpload save(ImageUpload imageUpload) {
			if (imageUpload.getId() == null) {
				imageUpload.assignId(sequence++);
			}
			uploads.put(imageUpload.getId(), imageUpload);
			return imageUpload;
		}

		@Override
		public Optional<ImageUpload> findByIdForUpdate(Long id) {
			return Optional.ofNullable(uploads.get(id));
		}
	}
}
