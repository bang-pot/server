package com.bangpot.image.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ImageUploadTest {

	private static final Instant NOW = Instant.parse("2026-05-17T00:00:00Z");

	@Test
	void createsTemporaryUpload() {
		ImageUpload upload = ImageUpload.createTemp(
			7L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"temp/log-photos/sample.jpg",
			1024L,
			NOW,
			NOW.plusSeconds(3600)
		);

		assertThat(upload.getUploaderUserId()).isEqualTo(7L);
		assertThat(upload.getCategory()).isEqualTo(ImageUploadCategory.MEETING_LOG_PHOTO);
		assertThat(upload.getStatus()).isEqualTo(ImageUploadStatus.TEMP);
		assertThat(upload.getTempKey()).isEqualTo("temp/log-photos/sample.jpg");
		assertThat(upload.getFinalKey()).isNull();
		assertThat(upload.getSizeBytes()).isEqualTo(1024L);
		assertThat(upload.getCreatedAt()).isEqualTo(NOW);
		assertThat(upload.getExpiresAt()).isEqualTo(NOW.plusSeconds(3600));
		assertThat(upload.getAttachedAt()).isNull();
	}

	@Test
	void attachesTemporaryUpload() {
		ImageUpload upload = ImageUpload.createTemp(
			7L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"temp/log-photos/sample.jpg",
			1024L,
			NOW,
			NOW.plusSeconds(3600)
		);
		Instant attachedAt = NOW.plusSeconds(60);

		upload.attach("log-photos/sample.jpg", attachedAt);

		assertThat(upload.getStatus()).isEqualTo(ImageUploadStatus.ATTACHED);
		assertThat(upload.getFinalKey()).isEqualTo("log-photos/sample.jpg");
		assertThat(upload.getAttachedAt()).isEqualTo(attachedAt);
	}

	@Test
	void expiresTemporaryUpload() {
		ImageUpload upload = ImageUpload.createTemp(
			7L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"temp/log-photos/sample.jpg",
			1024L,
			NOW,
			NOW.plusSeconds(3600)
		);

		upload.expire();

		assertThat(upload.getStatus()).isEqualTo(ImageUploadStatus.EXPIRED);
	}

	@Test
	void rejectsAttachAfterExpired() {
		ImageUpload upload = ImageUpload.createTemp(
			7L,
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"temp/log-photos/sample.jpg",
			1024L,
			NOW,
			NOW.plusSeconds(3600)
		);
		upload.expire();

		assertThatThrownBy(() -> upload.attach("log-photos/sample.jpg", NOW.plusSeconds(60)))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("임시 업로드 상태에서만 이미지를 확정할 수 있습니다.");
	}
}
