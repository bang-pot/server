package com.bangpot.common.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {

	private final FileStorageProperties fileStorageProperties;

	@Override
	public StoredFile store(String baseUrl, String category, MultipartFile file) {
		Path directory = fileStorageProperties.rootDirectoryPath().resolve(category);
		try {
			Files.createDirectories(directory);
			String extension = extractExtension(file.getOriginalFilename());
			String storedName = UUID.randomUUID() + "." + extension;
			Path destination = directory.resolve(storedName);
			Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
			return new StoredFile(
				baseUrl + "/uploads/" + category + "/" + storedName,
				file.getSize(),
				storedName
			);
		} catch (IOException exception) {
			throw new IllegalStateException("failed to store file", exception);
		}
	}

	private String extractExtension(String originalFilename) {
		if (originalFilename == null) {
			return "";
		}
		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
			return "";
		}
		return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}
}
