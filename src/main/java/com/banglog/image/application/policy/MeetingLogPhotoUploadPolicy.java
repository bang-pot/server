package com.banglog.image.application.policy;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.banglog.image.domain.ImageUploadCategory;

@Component
public class MeetingLogPhotoUploadPolicy extends AbstractImageUploadPolicy {

	public MeetingLogPhotoUploadPolicy() {
		super(
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"temp/log-photos",
			"log-photos",
			5L * 1024 * 1024,
			5,
			Set.of("jpg", "jpeg", "png"),
			Set.of("image/jpeg", "image/png"),
			"방탈로그 사진 파일은 비어 있을 수 없습니다.",
			"방탈로그 사진은 jpg, jpeg, png 형식만 업로드할 수 있습니다.",
			"방탈로그 사진은 5MB를 초과할 수 없습니다.",
			"방탈로그 사진은 최대 5개까지 첨부할 수 있습니다."
		);
	}
}
