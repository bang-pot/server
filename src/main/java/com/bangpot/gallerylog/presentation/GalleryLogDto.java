package com.bangpot.gallerylog.presentation;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

final class GalleryLogDto {

	private GalleryLogDto() {
	}

	record PhotoRequest(
		@NotBlank(message = "사진 URL은 필수입니다.") String url,
		@NotNull(message = "사진 크기 정보는 필수입니다.") Long sizeBytes
	) {
	}

	record CreateMeetingLogRequest(
		@NotBlank(message = "본문은 필수입니다.") String body,
		@Valid List<PhotoRequest> photos
	) {
	}

	record UpdateMeetingLogRequest(
		@NotBlank(message = "본문은 필수입니다.") String body,
		@Valid List<PhotoRequest> photos
	) {
	}

	record MeetingLogWriteResponse(
		Long logId,
		Long meetingId
	) {
	}

	record MeetingLogDeleteResponse(Long logId) {
	}

	record MeetingLogDetailResponse(
		Long logId,
		Long meetingId,
		String meetingTitle,
		String themeName,
		String place,
		String date,
		String authorNickname,
		Instant createdAt,
		Instant updatedAt,
		String body,
		List<String> photos
	) {
	}
}
