package com.bangpot.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class S3FileStorageTest {

	@Test
	void storesObjectAndReturnsPublicUrlAndKey() {
		S3Client s3Client = mock(S3Client.class);
		FileStorageProperties properties = new FileStorageProperties();
		properties.setPublicBaseUrl("https://banglog-image.s3.ap-northeast-2.amazonaws.com");
		properties.getS3().setBucket("banglog-image");
		S3FileStorage storage = new S3FileStorage(s3Client, properties);
		MockMultipartFile file = new MockMultipartFile("file", "sample.png", "image/png", "image-bytes".getBytes());

		StoredFile stored = storage.store("log-photos", file);

		assertThat(stored.key()).startsWith("log-photos/");
		assertThat(stored.key()).endsWith(".png");
		assertThat(stored.url()).isEqualTo(
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/" + stored.key()
		);
		assertThat(stored.sizeBytes()).isEqualTo(file.getSize());
		assertThat(stored.storedName()).endsWith(".png");
		PutObjectRequest expectedRequest = PutObjectRequest.builder()
			.bucket("banglog-image")
			.key(stored.key())
			.contentType(file.getContentType())
			.contentLength(file.getSize())
			.build();
		verify(s3Client).putObject(
			eq(expectedRequest),
			any(RequestBody.class)
		);
	}

	@Test
	void rejectsMissingRequiredS3Properties() {
		S3Client s3Client = mock(S3Client.class);
		FileStorageProperties properties = new FileStorageProperties();
		S3FileStorage storage = new S3FileStorage(s3Client, properties);
		MockMultipartFile file = new MockMultipartFile("file", "sample.png", "image/png", "image-bytes".getBytes());

		assertThatThrownBy(() -> storage.store("log-photos", file))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("S3 저장소 설정이 필요합니다.");
	}
}
