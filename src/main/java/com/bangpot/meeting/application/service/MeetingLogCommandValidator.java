package com.bangpot.meeting.application.service;

import java.util.ArrayList;
import java.util.List;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;

final class MeetingLogCommandValidator {

	private static final int MAX_BODY_LENGTH = 1000;
	private static final int MAX_PHOTO_COUNT = 5;

	private MeetingLogCommandValidator() {
	}

	static void validate(String body, List<CreateMeetingLogUseCase.PhotoInput> photos) {
		validateInternal(body, photos == null ? List.of() : photos.stream()
			.map(photo -> photo == null ? null : photo.uploadId())
			.toList());
	}

	static void validateForUpdate(String body, List<UpdateMeetingLogUseCase.PhotoInput> photos) {
		validateInternal(body, photos == null ? List.of() : photos.stream()
			.map(photo -> photo == null ? null : photo.uploadId())
			.toList());
	}

	private static void validateInternal(String body, List<Long> uploadIds) {
		List<ApiErrorField> fieldErrors = new ArrayList<>();
		if (body == null || body.isBlank()) {
			fieldErrors.add(new ApiErrorField("body", "본문은 비어 있을 수 없습니다."));
		} else if (body.length() > MAX_BODY_LENGTH) {
			fieldErrors.add(new ApiErrorField("body", "본문은 1000자를 초과할 수 없습니다."));
		}

		if (uploadIds.size() > MAX_PHOTO_COUNT) {
			fieldErrors.add(new ApiErrorField("photos", "사진은 최대 5개까지 첨부할 수 있습니다."));
		}

		for (int index = 0; index < uploadIds.size(); index++) {
			if (uploadIds.get(index) == null) {
				fieldErrors.add(new ApiErrorField(
					"photos[" + index + "].uploadId",
					"사진 업로드 ID는 비어 있을 수 없습니다."
				));
			}
		}

		if (!fieldErrors.isEmpty()) {
			throw new MeetingLogRequestValidationException(fieldErrors);
		}
	}
}
