package com.bangpot.image.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.image.application.port.ImageUploadRepository;
import com.bangpot.image.domain.ImageUpload;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaImageUploadRepository implements ImageUploadRepository {

	private final ImageUploadJpaRepository imageUploadJpaRepository;

	@Override
	public ImageUpload save(ImageUpload imageUpload) {
		return imageUploadJpaRepository.save(imageUpload);
	}

	@Override
	public Optional<ImageUpload> findByIdForUpdate(Long id) {
		return imageUploadJpaRepository.findByIdForUpdate(id);
	}
}
