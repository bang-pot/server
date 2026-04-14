package com.bangpot.meeting.presentation;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

final class MeetingLogDto {

	private MeetingLogDto() {
	}

	record PhotoRequest(
		@NotBlank(message = "사진 URL은 비어 있을 수 없습니다.") String url,
		@NotNull(message = "사진 용량은 비어 있을 수 없습니다.") Long sizeBytes
	) {
	}

	record CreateMeetingLogRequest(
		@NotBlank(message = "본문은 비어 있을 수 없습니다.") String body,
		@Valid List<PhotoRequest> photos
	) {
	}

	record UpdateMeetingLogRequest(
		@NotBlank(message = "본문은 비어 있을 수 없습니다.") String body,
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

	record MeetingLogPhotoUploadResponse(
		String url,
		Long sizeBytes
	) {
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

