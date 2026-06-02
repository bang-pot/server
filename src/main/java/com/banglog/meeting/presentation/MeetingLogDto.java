package com.banglog.meeting.presentation;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

final class MeetingLogDto {

	private MeetingLogDto() {
	}

	record PhotoRequest(
		@NotNull(message = "사진 업로드 ID는 비어 있을 수 없습니다.") Long uploadId
	) {
	}

	record CreateMeetingLogRequest(
		@NotBlank(message = "본문은 비어 있을 수 없습니다.") String body,
		@NotBlank(message = "결과는 비어 있을 수 없습니다.")
		@Pattern(regexp = "SUCCESS|FAILURE", message = "결과는 SUCCESS 또는 FAILURE만 입력할 수 있습니다.") String result,
		@Valid List<PhotoRequest> photos
	) {
	}

	record UpdateMeetingLogRequest(
		@NotBlank(message = "본문은 비어 있을 수 없습니다.") String body,
		@NotBlank(message = "결과는 비어 있을 수 없습니다.")
		@Pattern(regexp = "SUCCESS|FAILURE", message = "결과는 SUCCESS 또는 FAILURE만 입력할 수 있습니다.") String result,
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
		String result,
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
		String result,
		String body,
		List<String> photos
	) {
	}
}
