package com.banglog.image.application.port;

import java.util.Optional;

import com.banglog.image.domain.ImageUpload;

public interface ImageUploadRepository {

	ImageUpload save(ImageUpload imageUpload);

	Optional<ImageUpload> findByIdForUpdate(Long id);
}
