package com.banglog.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class S3FileStorageTest {

	@Test
	void storesObjectAndReturnsKey() {
		S3Client s3Client = mock(S3Client.class);
		FileStorageProperties properties = properties();
		S3FileStorage storage = new S3FileStorage(s3Client, properties);
		MockMultipartFile file = new MockMultipartFile("file", "sample.png", "image/png", "image-bytes".getBytes());

		String key = storage.store("log-photos", file);

		assertThat(key).startsWith("log-photos/");
		assertThat(key).endsWith(".png");
		PutObjectRequest expectedRequest = PutObjectRequest.builder()
			.bucket("banglog-image")
			.key(key)
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

	@Test
	void throwsKoreanMessageWhenFileStreamCannotBeRead() throws IOException {
		S3Client s3Client = mock(S3Client.class);
		S3FileStorage storage = new S3FileStorage(s3Client, properties());
		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("sample.png");
		when(file.getContentType()).thenReturn("image/png");
		when(file.getSize()).thenReturn(10L);
		when(file.getInputStream()).thenThrow(new IOException("stream error"));

		assertThatThrownBy(() -> storage.store("log-photos", file))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("파일 저장에 실패했습니다.");
	}

	@Test
	void copiesObjectInsideBucket() {
		S3Client s3Client = mock(S3Client.class);
		S3FileStorage storage = new S3FileStorage(s3Client, properties());

		storage.copy("temp/log-photos/a.jpg", "log-photos/a.jpg");

		verify(s3Client).copyObject(CopyObjectRequest.builder()
			.sourceBucket("banglog-image")
			.sourceKey("temp/log-photos/a.jpg")
			.destinationBucket("banglog-image")
			.destinationKey("log-photos/a.jpg")
			.build());
	}

	@Test
	void deletesObject() {
		S3Client s3Client = mock(S3Client.class);
		S3FileStorage storage = new S3FileStorage(s3Client, properties());

		storage.delete("temp/log-photos/a.jpg");

		verify(s3Client).deleteObject(DeleteObjectRequest.builder()
			.bucket("banglog-image")
			.key("temp/log-photos/a.jpg")
			.build());
	}

	private FileStorageProperties properties() {
		FileStorageProperties properties = new FileStorageProperties();
		properties.setPublicBaseUrl("https://banglog-image.s3.ap-northeast-2.amazonaws.com");
		properties.getS3().setBucket("banglog-image");
		return properties;
	}
}
