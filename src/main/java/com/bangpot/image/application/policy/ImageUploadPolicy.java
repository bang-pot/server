package com.bangpot.image.application.policy;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bangpot.image.domain.ImageUploadCategory;

public interface ImageUploadPolicy {

	ImageUploadCategory category();

	default boolean supports(ImageUploadCategory targetCategory) {
		return category() == targetCategory;
	}

	String tempDirectory();

	String finalDirectory();

	void validateTemporaryUpload(MultipartFile file);

	void validateAttach(List<Long> uploadIds);
}
