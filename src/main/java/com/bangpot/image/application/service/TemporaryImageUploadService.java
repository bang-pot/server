package com.bangpot.image.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorage;
import com.bangpot.image.application.exception.ImageUploadRequestValidationException;
import com.bangpot.image.application.port.ImageUploadRepository;
import com.bangpot.image.application.usecase.UploadTemporaryImageUseCase;
import com.bangpot.image.domain.ImageUpload;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemporaryImageUploadService implements UploadTemporaryImageUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "완료된 사용자만 이미지를 업로드할 수 있습니다.";
	private static final String FILE_FIELD = "file";
	private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
	private static final Duration TEMP_UPLOAD_TTL = Duration.ofHours(24);
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

	private final CompletedUserAccessService completedUserAccessService;
	private final ImageUploadRepository imageUploadRepository;
	private final FileStorage fileStorage;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), COMPLETED_USER_REQUIRED_MESSAGE);
		ImageUpload imageUpload = uploadTemporary(command);
		return Result.of(
			imageUpload.getId(),
			fileStorage.publicUrl(imageUpload.getTempKey()),
			imageUpload.getSizeBytes()
		);
	}

	private ImageUpload uploadTemporary(Command command) {
		validate(command.file());

		String tempKey = fileStorage.store(command.tempDirectory(), command.file());
		return imageUploadRepository.save(toTemporaryUpload(command, tempKey));
	}

	private ImageUpload toTemporaryUpload(Command command, String tempKey) {
		Instant now = clock.instant();
		return ImageUpload.createTemp(
			command.userId(),
			command.category(),
			tempKey,
			command.file().getSize(),
			now,
			now.plus(TEMP_UPLOAD_TTL)
		);
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw validationError(FILE_FIELD, "사진 파일은 비어 있을 수 없습니다.");
		}
		String extension = extractExtension(file.getOriginalFilename());
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw validationError(FILE_FIELD, "사진은 jpg, jpeg, png 형식만 허용합니다.");
		}
		if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
			throw validationError("file.contentType", "사진은 jpg, jpeg, png 형식만 허용합니다.");
		}
		if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
			throw validationError("file.sizeBytes", "사진은 5MB를 초과할 수 없습니다.");
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

	private ImageUploadRequestValidationException validationError(String field, String message) {
		return new ImageUploadRequestValidationException(List.of(new ApiErrorField(field, message)));
	}
}
