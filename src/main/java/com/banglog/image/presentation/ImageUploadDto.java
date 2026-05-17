package com.banglog.image.presentation;

final class ImageUploadDto {

	private ImageUploadDto() {
	}

	record ImageUploadResponse(
		Long uploadId,
		String url,
		Long sizeBytes
	) {
	}
}
