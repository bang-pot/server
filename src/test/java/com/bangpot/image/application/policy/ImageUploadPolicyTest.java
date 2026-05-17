package com.bangpot.image.application.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.bangpot.image.application.exception.ImageUploadRequestValidationException;

class ImageUploadPolicyTest {

	@Test
	void rejectsUnsupportedExtension() {
		MeetingLogPhotoUploadPolicy policy = new MeetingLogPhotoUploadPolicy();
		MockMultipartFile file = new MockMultipartFile("file", "photo.gif", "image/gif", "image".getBytes());

		assertThatThrownBy(() -> policy.validateTemporaryUpload(file))
			.isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertFieldError(
				(ImageUploadRequestValidationException)exception,
				"file",
				"방탈로그 사진은 jpg, jpeg, png 형식만 업로드할 수 있습니다."
			));
	}

	@Test
	void rejectsUnsupportedContentType() {
		MeetingLogPhotoUploadPolicy policy = new MeetingLogPhotoUploadPolicy();
		MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "text/plain", "image".getBytes());

		assertThatThrownBy(() -> policy.validateTemporaryUpload(file))
			.isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertFieldError(
				(ImageUploadRequestValidationException)exception,
				"file.contentType",
				"방탈로그 사진은 jpg, jpeg, png 형식만 업로드할 수 있습니다."
			));
	}

	@Test
	void rejectsOversizedProfileImage() {
		ProfileImageUploadPolicy policy = new ProfileImageUploadPolicy();
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getOriginalFilename()).thenReturn("profile.jpg");
		when(file.getContentType()).thenReturn("image/jpeg");
		when(file.getSize()).thenReturn(5L * 1024 * 1024 + 1);

		assertThatThrownBy(() -> policy.validateTemporaryUpload(file))
			.isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertFieldError(
				(ImageUploadRequestValidationException)exception,
				"file.sizeBytes",
				"프로필 이미지는 5MB를 초과할 수 없습니다."
			));
	}

	@Test
	void rejectsTooManyMeetingLogPhotos() {
		MeetingLogPhotoUploadPolicy policy = new MeetingLogPhotoUploadPolicy();

		assertThatThrownBy(() -> policy.validateAttach(List.of(1L, 2L, 3L, 4L, 5L, 6L)))
			.isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertFieldError(
				(ImageUploadRequestValidationException)exception,
				"uploadIds",
				"방탈로그 사진은 최대 5개까지 첨부할 수 있습니다."
			));
	}

	@Test
	void rejectsMultipleCrewCoverImages() {
		CrewCoverImageUploadPolicy policy = new CrewCoverImageUploadPolicy();

		assertThatThrownBy(() -> policy.validateAttach(List.of(1L, 2L)))
			.isInstanceOf(ImageUploadRequestValidationException.class)
			.satisfies(exception -> assertFieldError(
				(ImageUploadRequestValidationException)exception,
				"uploadIds",
				"크루 커버 이미지는 1개만 설정할 수 있습니다."
			));
	}

	private void assertFieldError(
		ImageUploadRequestValidationException exception,
		String field,
		String message
	) {
		assertThat(exception.getFieldErrors())
			.extracting("field", "message")
			.containsExactly(org.assertj.core.groups.Tuple.tuple(field, message));
	}
}
