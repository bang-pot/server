package com.banglog.image.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banglog.auth.presentation.UnauthenticatedException;
import com.banglog.image.application.usecase.UploadTemporaryImageUseCase;
import com.banglog.image.domain.ImageUploadCategory;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
class ImageUploadController {

	private final UploadTemporaryImageUseCase uploadTemporaryImageUseCase;

	@PostMapping("/uploads/log-photos")
	ResponseEntity<ImageUploadDto.ImageUploadResponse> uploadLogPhoto(
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		return upload(authentication, ImageUploadCategory.MEETING_LOG_PHOTO, file);
	}

	@PostMapping("/uploads/profile-images")
	ResponseEntity<ImageUploadDto.ImageUploadResponse> uploadProfileImage(
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		return upload(authentication, ImageUploadCategory.PROFILE_IMAGE, file);
	}

	@PostMapping("/uploads/crew-cover-images")
	ResponseEntity<ImageUploadDto.ImageUploadResponse> uploadCrewCoverImage(
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		return upload(authentication, ImageUploadCategory.CREW_COVER_IMAGE, file);
	}

	private ResponseEntity<ImageUploadDto.ImageUploadResponse> upload(
		Authentication authentication,
		ImageUploadCategory category,
		MultipartFile file
	) {
		return ResponseEntity.ok(
			ImageUploadDtoMapper.toResponse(
				uploadTemporaryImageUseCase.handle(
					UploadTemporaryImageUseCase.Command.of(
						requireAuthenticatedUserId(authentication),
						category,
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
