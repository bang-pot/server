package com.banglog.meeting.presentation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

final class MeetingDto {

	private MeetingDto() {
	}

	record CreateMeetingRequest(
		@NotBlank(message = "제목은 필수입니다.") String title,
		@NotBlank(message = "모임 날짜는 필수입니다.") String date,
		@NotBlank(message = "모임 시간은 필수입니다.") String time,
		@NotBlank(message = "장소는 필수입니다.") String place,
		@NotBlank(message = "테마명은 필수입니다.") String themeName,
		@NotNull(message = "정원은 필수입니다.")
		@Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description
	) {
	}

	record UpdateMeetingRequest(
		@NotBlank(message = "제목은 필수입니다.") String title,
		@NotBlank(message = "모임 날짜는 필수입니다.") String date,
		@NotBlank(message = "모임 시간은 필수입니다.") String time,
		@NotBlank(message = "장소는 필수입니다.") String place,
		@NotBlank(message = "테마명은 필수입니다.") String themeName,
		@NotNull(message = "정원은 필수입니다.")
		@Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description
	) {
	}

	record CreateMeetingResponse(
		Long meetingId,
		Long crewId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		String status
	) {
	}

	record MeetingListResponse(
		Long meetingId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		String status,
		Long participantCount,
		Integer capacity
	) {
	}

	record MeetingListPageResponse(
		java.util.List<MeetingListResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	record PageInfoResponse(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record MeetingDetailResponse(
		Long meetingId,
		Long crewId,
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		Integer capacity,
		Long participantCount,
		Integer totalCost,
		String contactLink,
		String description,
		String status,
		String myParticipationStatus
	) {
	}

	record MeetingJoinResponse(
		Long meetingId,
		String myParticipationStatus
	) {
	}

	record MeetingStatusChangeResponse(
		Long meetingId,
		String status
	) {
	}

	record UpdateMeetingResponse(
		Long meetingId,
		Long crewId,
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String date,
		String time,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description,
		String status
	) {
	}
}
