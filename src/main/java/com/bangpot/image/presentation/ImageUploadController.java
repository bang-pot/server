package com.bangpot.image.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.image.application.usecase.UploadTemporaryImageUseCase;
import com.bangpot.image.domain.ImageUploadCategory;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
class ImageUploadController {

	private static final String TEMP_LOG_PHOTO_DIRECTORY = "temp/log-photos";

	private final UploadTemporaryImageUseCase uploadTemporaryImageUseCase;

	@PostMapping("/uploads/log-photos")
	ResponseEntity<ImageUploadDto.ImageUploadResponse> uploadLogPhoto(
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		return ResponseEntity.ok(
			ImageUploadDtoMapper.toResponse(
				uploadTemporaryImageUseCase.handle(
					UploadTemporaryImageUseCase.Command.of(
						requireAuthenticatedUserId(authentication),
						ImageUploadCategory.MEETING_LOG_PHOTO,
						TEMP_LOG_PHOTO_DIRECTORY,
						file
					)
				)
			)
		);
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
