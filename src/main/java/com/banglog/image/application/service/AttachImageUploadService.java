package com.banglog.image.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.common.error.ApiErrorField;
import com.banglog.common.storage.FileStorage;
import com.banglog.image.application.exception.ImageUploadRequestValidationException;
import com.banglog.image.application.policy.ImageUploadPolicy;
import com.banglog.image.application.port.ImageUploadRepository;
import com.banglog.image.application.usecase.AttachImageUploadUseCase;
import com.banglog.image.domain.ImageUpload;
import com.banglog.image.domain.ImageUploadCategory;
import com.banglog.image.domain.ImageUploadStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachImageUploadService implements AttachImageUploadUseCase {

	private static final String UNSUPPORTED_CATEGORY_MESSAGE = "지원하지 않는 이미지 업로드 유형입니다.";

	private final ImageUploadRepository imageUploadRepository;
	private final FileStorage fileStorage;
	private final List<ImageUploadPolicy> policies;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		return Result.of(attach(
			command.userId(),
			command.category(),
			command.uploadIds()
		));
	}

	private List<String> attach(
		Long userId,
		ImageUploadCategory category,
		List<Long> uploadIds
	) {
		if (uploadIds == null || uploadIds.isEmpty()) {
			return List.of();
		}

		ImageUploadPolicy policy = findPolicy(category);
		policy.validateAttach(uploadIds);

		Instant now = clock.instant();
		List<String> imageUrls = new ArrayList<>();
		for (int index = 0; index < uploadIds.size(); index++) {
			ImageUpload upload = findUpload(uploadIds.get(index), index);
			validateAttachable(upload, userId, category, index, now);
			String finalKey = finalKey(policy.finalDirectory(), upload.getTempKey(), index);
			upload.attach(finalKey, now);
			fileStorage.copy(upload.getTempKey(), finalKey);
			fileStorage.delete(upload.getTempKey());
			imageUrls.add(fileStorage.publicUrl(finalKey));
		}
		return imageUrls;
	}

	private ImageUploadPolicy findPolicy(ImageUploadCategory category) {
		return policies.stream()
			.filter(policy -> policy.supports(category))
			.findFirst()
			.orElseThrow(() -> validationError("category", UNSUPPORTED_CATEGORY_MESSAGE));
	}

	private ImageUpload findUpload(Long uploadId, int index) {
		if (uploadId == null) {
			throw validationError("uploadIds[" + index + "]", "첨부할 사진을 선택해 주세요.");
		}
		return imageUploadRepository.findByIdForUpdate(uploadId)
			.orElseThrow(() -> validationError("uploadIds[" + index + "]", "첨부할 수 없는 사진입니다."));
	}

	private void validateAttachable(
		ImageUpload upload,
		Long userId,
		ImageUploadCategory category,
		int index,
		Instant now
	) {
		String field = "uploadIds[" + index + "]";
		if (!upload.getUploaderUserId().equals(userId)) {
			throw validationError(field, "본인이 업로드한 사진만 첨부할 수 있습니다.");
		}
		if (upload.getCategory() != category) {
			throw validationError(field, "첨부할 수 없는 이미지 유형입니다.");
		}
		if (upload.getStatus() != ImageUploadStatus.TEMP) {
			throw validationError(field, "이미 사용된 사진입니다. 다시 업로드해 주세요.");
		}
		if (!upload.getExpiresAt().isAfter(now)) {
			throw validationError(field, "만료된 사진입니다. 다시 업로드해 주세요.");
		}
	}

	private String finalKey(String finalDirectory, String tempKey, int index) {
		if (finalDirectory == null || finalDirectory.isBlank()) {
			throw validationError("finalDirectory", "이미지 저장 경로는 비어 있을 수 없습니다.");
		}
		if (tempKey == null || tempKey.isBlank() || !tempKey.startsWith("temp/")) {
			throw validationError("uploadIds[" + index + "]", "첨부할 수 없는 사진입니다.");
		}
		String storedName = tempKey.substring(tempKey.lastIndexOf('/') + 1);
		if (storedName.isBlank()) {
			throw validationError("uploadIds[" + index + "]", "첨부할 수 없는 사진입니다.");
		}
		return finalDirectory + "/" + storedName;
	}

	private ImageUploadRequestValidationException validationError(String field, String message) {
		return new ImageUploadRequestValidationException(List.of(new ApiErrorField(field, message)));
	}
}
