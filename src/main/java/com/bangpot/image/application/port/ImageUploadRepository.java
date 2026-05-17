package com.bangpot.image.application.port;

import java.util.Optional;

import com.bangpot.image.domain.ImageUpload;

public interface ImageUploadRepository {

	ImageUpload save(ImageUpload imageUpload);

	Optional<ImageUpload> findByIdForUpdate(Long id);
}
