package com.banglog.common.storage;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

	private final S3Client s3Client;
	private final FileStorageProperties fileStorageProperties;

	@Override
	public String store(String category, MultipartFile file) {
		validateRequiredProperties();
		String key = category + "/" + UUID.randomUUID() + "." + extractExtension(file.getOriginalFilename());
		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(fileStorageProperties.getS3().getBucket())
			.key(key)
			.contentType(file.getContentType())
			.contentLength(file.getSize())
			.build();
		try {
			s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
			return key;
		} catch (IOException exception) {
			throw new IllegalStateException("파일 저장에 실패했습니다.", exception);
		}
	}

	@Override
	public void copy(String sourceKey, String targetKey) {
		validateRequiredProperties();
		CopyObjectRequest request = CopyObjectRequest.builder()
			.sourceBucket(fileStorageProperties.getS3().getBucket())
			.sourceKey(sourceKey)
			.destinationBucket(fileStorageProperties.getS3().getBucket())
			.destinationKey(targetKey)
			.build();
		s3Client.copyObject(request);
	}

	@Override
	public void delete(String key) {
		validateRequiredProperties();
		DeleteObjectRequest request = DeleteObjectRequest.builder()
			.bucket(fileStorageProperties.getS3().getBucket())
			.key(key)
			.build();
		s3Client.deleteObject(request);
	}

	@Override
	public String publicUrl(String key) {
		String publicBaseUrl = fileStorageProperties.getPublicBaseUrl();
		if (publicBaseUrl.endsWith("/")) {
			return publicBaseUrl + key;
		}
		return publicBaseUrl + "/" + key;
	}

	private void validateRequiredProperties() {
		if (isBlank(fileStorageProperties.getS3().getBucket()) || isBlank(fileStorageProperties.getPublicBaseUrl())) {
			throw new IllegalStateException("S3 저장소 설정이 필요합니다.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
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
