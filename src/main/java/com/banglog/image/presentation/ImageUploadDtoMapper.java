package com.banglog.image.presentation;

import com.banglog.image.application.usecase.UploadTemporaryImageUseCase;

final class ImageUploadDtoMapper {

	private ImageUploadDtoMapper() {
	}

	static ImageUploadDto.ImageUploadResponse toResponse(UploadTemporaryImageUseCase.Result result) {
		return new ImageUploadDto.ImageUploadResponse(result.uploadId(), result.url(), result.sizeBytes());
	}
}
