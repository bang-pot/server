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
		@NotNull(message = "사진 업로드 ID는 비어 있을 수 없습니다.") Long uploadId
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

	record DeleteMeetingLogRequest(
		String deleteReason
	) {
	}

	record MeetingLogWriteResponse(
		Long logId,
		Long meetingId
	) {
	}

	record MeetingLogDeleteResponse(
		Long logId,
		String deletedBy
	) {
	}

	record MyMeetingLogResponse(
		String status,
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
