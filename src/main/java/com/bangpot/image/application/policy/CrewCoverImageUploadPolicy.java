package com.bangpot.image.application.policy;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.bangpot.image.domain.ImageUploadCategory;

@Component
public class CrewCoverImageUploadPolicy extends AbstractImageUploadPolicy {

	public CrewCoverImageUploadPolicy() {
		super(
			ImageUploadCategory.CREW_COVER_IMAGE,
			"temp/crew-cover-images",
			"crew-cover-images",
			5L * 1024 * 1024,
			1,
			Set.of("jpg", "jpeg", "png"),
			Set.of("image/jpeg", "image/png"),
			"크루 커버 이미지 파일은 비어 있을 수 없습니다.",
			"크루 커버 이미지는 jpg, jpeg, png 형식만 업로드할 수 있습니다.",
			"크루 커버 이미지는 5MB를 초과할 수 없습니다.",
			"크루 커버 이미지는 1개만 설정할 수 있습니다."
		);
	}
}
