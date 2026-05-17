package com.bangpot.image.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorage;
import com.bangpot.image.application.exception.ImageUploadRequestValidationException;
import com.bangpot.image.application.policy.ImageUploadPolicy;
import com.bangpot.image.application.port.ImageUploadRepository;
import com.bangpot.image.application.usecase.UploadTemporaryImageUseCase;
import com.bangpot.image.domain.ImageUpload;
import com.bangpot.image.domain.ImageUploadCategory;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemporaryImageUploadService implements UploadTemporaryImageUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "완료된 사용자만 이미지를 업로드할 수 있습니다.";
	private static final String UNSUPPORTED_CATEGORY_MESSAGE = "지원하지 않는 이미지 업로드 유형입니다.";
	private static final Duration TEMP_UPLOAD_TTL = Duration.ofHours(24);

	private final CompletedUserAccessService completedUserAccessService;
	private final ImageUploadRepository imageUploadRepository;
	private final FileStorage fileStorage;
	private final List<ImageUploadPolicy> policies;
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
		ImageUploadPolicy policy = findPolicy(command.category());
		policy.validateTemporaryUpload(command.file());

		String tempKey = fileStorage.store(policy.tempDirectory(), command.file());
		return imageUploadRepository.save(toTemporaryUpload(command, tempKey));
	}

	private ImageUploadPolicy findPolicy(ImageUploadCategory category) {
		return policies.stream()
			.filter(policy -> policy.supports(category))
			.findFirst()
			.orElseThrow(() -> new ImageUploadRequestValidationException(
				List.of(new ApiErrorField("category", UNSUPPORTED_CATEGORY_MESSAGE))
			));
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
}
