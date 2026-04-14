package com.bangpot.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageTest {

	@TempDir
	Path tempDir;

	@Test
	void storesFileUnderCategoryAndReturnsUrlAndSize() throws Exception {
		var properties = new FileStorageProperties();
		properties.setRootDirectory(tempDir.toString());
		var storage = new LocalFileStorage(properties);
		var file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "image-bytes".getBytes());

		var stored = storage.store("http://localhost:8080", "log-photos", file);

		assertThat(stored.sizeBytes()).isEqualTo(file.getSize());
		assertThat(stored.url()).startsWith("http://localhost:8080/uploads/log-photos/");
		assertThat(stored.storedName()).endsWith(".jpg");
		assertThat(Files.list(tempDir.resolve("log-photos")).count()).isEqualTo(1);
	}
}
