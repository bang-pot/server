package com.bangpot.image.application.policy;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.image.application.exception.ImageUploadRequestValidationException;
import com.bangpot.image.domain.ImageUploadCategory;

abstract class AbstractImageUploadPolicy implements ImageUploadPolicy {

	private static final String FILE_FIELD = "file";

	private final ImageUploadCategory category;
	private final String tempDirectory;
	private final String finalDirectory;
	private final long maxSizeBytes;
	private final int maxAttachCount;
	private final Set<String> allowedExtensions;
	private final Set<String> allowedContentTypes;
	private final String emptyFileMessage;
	private final String invalidTypeMessage;
	private final String maxSizeMessage;
	private final String maxAttachCountMessage;

	AbstractImageUploadPolicy(
		ImageUploadCategory category,
		String tempDirectory,
		String finalDirectory,
		long maxSizeBytes,
		int maxAttachCount,
		Set<String> allowedExtensions,
		Set<String> allowedContentTypes,
		String emptyFileMessage,
		String invalidTypeMessage,
		String maxSizeMessage,
		String maxAttachCountMessage
	) {
		this.category = category;
		this.tempDirectory = tempDirectory;
		this.finalDirectory = finalDirectory;
		this.maxSizeBytes = maxSizeBytes;
		this.maxAttachCount = maxAttachCount;
		this.allowedExtensions = allowedExtensions;
		this.allowedContentTypes = allowedContentTypes;
		this.emptyFileMessage = emptyFileMessage;
		this.invalidTypeMessage = invalidTypeMessage;
		this.maxSizeMessage = maxSizeMessage;
		this.maxAttachCountMessage = maxAttachCountMessage;
	}

	@Override
	public ImageUploadCategory category() {
		return category;
	}

	@Override
	public String tempDirectory() {
		return tempDirectory;
	}

	@Override
	public String finalDirectory() {
		return finalDirectory;
	}

	@Override
	public void validateTemporaryUpload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw validationError(FILE_FIELD, emptyFileMessage);
		}
		String extension = extractExtension(file.getOriginalFilename());
		if (!allowedExtensions.contains(extension)) {
			throw validationError(FILE_FIELD, invalidTypeMessage);
		}
		if (!allowedContentTypes.contains(file.getContentType())) {
			throw validationError("file.contentType", invalidTypeMessage);
		}
		if (file.getSize() > maxSizeBytes) {
			throw validationError("file.sizeBytes", maxSizeMessage);
		}
	}

	@Override
	public void validateAttach(List<Long> uploadIds) {
		if (uploadIds == null || uploadIds.isEmpty()) {
			return;
		}
		if (uploadIds.size() > maxAttachCount) {
			throw validationError("uploadIds", maxAttachCountMessage);
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

	protected ImageUploadRequestValidationException validationError(String field, String message) {
		return new ImageUploadRequestValidationException(List.of(new ApiErrorField(field, message)));
	}
}
