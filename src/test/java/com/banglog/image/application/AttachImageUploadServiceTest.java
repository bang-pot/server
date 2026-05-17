package com.banglog.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.doAnswer;
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
import org.springframework.transaction.annotation.Transactional;

import com.banglog.common.storage.FileStorage;
import com.banglog.image.application.exception.ImageUploadRequestValidationException;
import com.banglog.image.application.policy.MeetingLogPhotoUploadPolicy;
import com.banglog.image.application.port.ImageUploadRepository;
import com.banglog.image.application.service.AttachImageUploadService;
import com.banglog.image.application.usecase.AttachImageUploadUseCase;
import com.banglog.image.domain.ImageUpload;
import com.banglog.image.domain.ImageUploadCategory;
import com.banglog.image.domain.ImageUploadStatus;

class AttachImageUploadServiceTest {

	private static final Instant NOW = Instant.parse("2026-05-17T12:00:00Z");

	@Test
	void attachesImageUploadInTransaction() throws NoSuchMethodException {
		assertThat(AttachImageUploadService.class
			.getMethod("handle", AttachImageUploadUseCase.Command.class))
			.satisfies(method -> assertThat(method.isAnnotationPresent(Transactional.class)).isTrue());
	}

	@Test
	void attachesTemporaryImageUploadToFinalStorage() {
		InMemoryImageUploadRepository imageUploadRepository = new InMemoryImageUploadRepository();
		ImageUpload upload = tempUpload(1L, 10L, NOW.plusSeconds(3600));
		imageUploadRepository.save(upload);
		FileStorage fileStorage = mock(FileStorage.class);
		doAnswer(invocation -> {
			assertThat(upload.getStatus()).isEqualTo(ImageUploadStatus.ATTACHED);
			assertThat(upload.getFinalKey()).isEqualTo("log-photos/stored.jpg");
			return null;
		}).when(fileStorage).copy("temp/log-photos/stored.jpg", "log-photos/stored.jpg");
		when(fileStorage.publicUrl("log-photos/stored.jpg")).thenReturn("https://cdn.example.com/log-photos/stored.jpg");
		AttachImageUploadService service = service(imageUploadRepository, fileStorage);

		List<String> photoUrls = service.handle(AttachImageUploadUseCase.Command.of(
			10L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			List.of(1L)
		)).urls();

		assertThat(photoUrls).containsExactly("https://cdn.example.com/log-photos/stored.jpg");
		verify(fileStorage).copy("temp/log-photos/stored.jpg", "log-photos/stored.jpg");
		verify(fileStorage).delete("temp/log-photos/stored.jpg");
		assertThat(upload.getStatus()).isEqualTo(ImageUploadStatus.ATTACHED);
		assertThat(upload.getFinalKey()).isEqualTo("log-photos/stored.jpg");
		assertThat(upload.getAttachedAt()).isEqualTo(NOW);
		assertThat(imageUploadRepository.findByIdForUpdateCalled()).isTrue();
	}

	@Test
	void rejectsUploadOwnedByAnotherUser() {
		InMemoryImageUploadRepository imageUploadRepository = new InMemoryImageUploadRepository();
		imageUploadRepository.save(tempUpload(1L, 10L, NOW.plusSeconds(3600)));
		AttachImageUploadService service = service(imageUploadRepository, mock(FileStorage.class));

		assertThatThrownBy(() -> service.handle(AttachImageUploadUseCase.Command.of(
			99L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			List.of(1L)
		))).isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertThat(((ImageUploadRequestValidationException)exception).getFieldErrors())
				.extracting("field", "message")
				.containsExactly(tuple(
					"uploadIds[0]",
					"본인이 업로드한 사진만 첨부할 수 있습니다."
				)));
	}

	@Test
	void rejectsExpiredUpload() {
		InMemoryImageUploadRepository imageUploadRepository = new InMemoryImageUploadRepository();
		imageUploadRepository.save(tempUpload(1L, 10L, NOW.minusSeconds(1)));
		AttachImageUploadService service = service(imageUploadRepository, mock(FileStorage.class));

		assertThatThrownBy(() -> service.handle(AttachImageUploadUseCase.Command.of(
			10L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			List.of(1L)
		))).isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertThat(((ImageUploadRequestValidationException)exception).getFieldErrors())
				.extracting("field", "message")
				.containsExactly(tuple(
					"uploadIds[0]",
					"만료된 사진입니다. 다시 업로드해 주세요."
				)));
	}

	private AttachImageUploadService service(
		ImageUploadRepository imageUploadRepository,
		FileStorage fileStorage
	) {
		return new AttachImageUploadService(
			imageUploadRepository,
			fileStorage,
			List.of(new MeetingLogPhotoUploadPolicy()),
			Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	private ImageUpload tempUpload(Long id, Long uploaderUserId, Instant expiresAt) {
		ImageUpload upload = ImageUpload.createTemp(
			uploaderUserId,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"temp/log-photos/stored.jpg",
			1024L,
			NOW.minusSeconds(60),
			expiresAt
		);
		upload.assignId(id);
		return upload;
	}

	private static class InMemoryImageUploadRepository implements ImageUploadRepository {

		private final Map<Long, ImageUpload> uploads = new HashMap<>();
		private long sequence = 1L;
		private boolean findByIdForUpdateCalled;

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
			findByIdForUpdateCalled = true;
			return Optional.ofNullable(uploads.get(id));
		}

		boolean findByIdForUpdateCalled() {
			return findByIdForUpdateCalled;
		}
	}
}
