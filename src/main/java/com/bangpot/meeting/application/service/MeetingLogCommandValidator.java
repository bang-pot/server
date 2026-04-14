package com.bangpot.meeting.application.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;

final class MeetingLogCommandValidator {

	private static final int MAX_BODY_LENGTH = 1000;
	private static final int MAX_PHOTO_COUNT = 5;
	private static final long MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024;
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

	private MeetingLogCommandValidator() {
	}

	static void validate(String body, List<CreateMeetingLogUseCase.PhotoInput> photos) {
		validateInternal(body, photos == null ? List.of() : photos.stream()
			.map(photo -> new PhotoCandidate(photo.url(), photo.sizeBytes()))
			.toList());
	}

	static void validateForUpdate(String body, List<UpdateMeetingLogUseCase.PhotoInput> photos) {
		validateInternal(body, photos == null ? List.of() : photos.stream()
			.map(photo -> new PhotoCandidate(photo.url(), photo.sizeBytes()))
			.toList());
	}

	private static void validateInternal(String body, List<PhotoCandidate> safePhotos) {
		List<ApiErrorField> fieldErrors = new ArrayList<>();
		if (body == null || body.isBlank()) {
			fieldErrors.add(new ApiErrorField("body", "본문은 비어 있을 수 없습니다."));
		} else if (body.length() > MAX_BODY_LENGTH) {
			fieldErrors.add(new ApiErrorField("body", "본문은 1000자를 초과할 수 없습니다."));
		}

		if (safePhotos.size() > MAX_PHOTO_COUNT) {
			fieldErrors.add(new ApiErrorField("photos", "사진은 최대 5장까지만 첨부할 수 있습니다."));
		}

		for (int index = 0; index < safePhotos.size(); index++) {
			PhotoCandidate photo = safePhotos.get(index);
			String field = "photos[" + index + "]";
			if (photo == null || photo.url == null || photo.url.isBlank()) {
				fieldErrors.add(new ApiErrorField(field, "사진 URL은 비어 있을 수 없습니다."));
				continue;
			}
			if (!hasAllowedExtension(photo.url)) {
				fieldErrors.add(new ApiErrorField(field, "사진은 jpg, jpeg, png 형식만 허용됩니다."));
			}
			if (photo.sizeBytes == null || photo.sizeBytes <= 0) {
				fieldErrors.add(new ApiErrorField(field + ".sizeBytes", "사진 용량은 비어 있을 수 없습니다."));
			} else if (photo.sizeBytes > MAX_PHOTO_SIZE_BYTES) {
				fieldErrors.add(new ApiErrorField(field + ".sizeBytes", "사진은 5MB를 초과할 수 없습니다."));
			}
		}

		if (!fieldErrors.isEmpty()) {
			throw new MeetingLogRequestValidationException(fieldErrors);
		}
	}

	private static boolean hasAllowedExtension(String url) {
		String path = extractPath(url);
		int extensionStart = path.lastIndexOf('.');
		if (extensionStart < 0 || extensionStart == path.length() - 1) {
			return false;
		}
		String extension = path.substring(extensionStart + 1).toLowerCase(Locale.ROOT);
		return ALLOWED_EXTENSIONS.contains(extension);
	}

	private static String extractPath(String url) {
		try {
			return URI.create(url).getPath();
		} catch (IllegalArgumentException exception) {
			int queryStart = url.indexOf('?');
			String withoutQuery = queryStart >= 0 ? url.substring(0, queryStart) : url;
			int fragmentStart = withoutQuery.indexOf('#');
			return fragmentStart >= 0 ? withoutQuery.substring(0, fragmentStart) : withoutQuery;
		}
	}

	private record PhotoCandidate(String url, Long sizeBytes) {
	}
}

