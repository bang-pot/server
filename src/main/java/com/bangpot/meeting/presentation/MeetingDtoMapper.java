package com.bangpot.meeting.presentation;

import java.util.List;

import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;

final class MeetingDtoMapper {

	private MeetingDtoMapper() {
	}

	static CreateMeetingUseCase.Command toCommand(Long crewId, Long userId, MeetingDto.CreateMeetingRequest request) {
		return CreateMeetingUseCase.Command.of(
			crewId,
			userId,
			request.date(),
			request.time(),
			request.place(),
			request.themeName(),
			request.capacity(),
			request.totalCost(),
			request.reservationLink(),
			request.openChatLink(),
			request.description()
		);
	}

	static MeetingDto.CreateMeetingResponse toResponse(CreateMeetingUseCase.Result result) {
		return new MeetingDto.CreateMeetingResponse(
			result.meetingId(),
			result.crewId(),
			result.themeName(),
			result.place(),
			result.date(),
			result.time(),
			result.status(),
			result.result()
		);
	}

	static GetMeetingsUseCase.Query toQuery(Long crewId, Long userId) {
		return GetMeetingsUseCase.Query.of(crewId, userId);
	}

	static List<MeetingDto.MeetingListResponse> toListResponses(List<GetMeetingsUseCase.View> views) {
		return views.stream()
			.map(view -> new MeetingDto.MeetingListResponse(
				view.meetingId(),
				view.themeName(),
				view.place(),
				view.date(),
				view.time(),
				view.status(),
				view.result(),
				view.capacity()
			))
			.toList();
	}

	static GetMeetingDetailUseCase.Query toQuery(Long crewId, Long meetingId, Long userId) {
		return GetMeetingDetailUseCase.Query.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingDetailResponse toResponse(GetMeetingDetailUseCase.Result result) {
		return new MeetingDto.MeetingDetailResponse(
			result.meetingId(),
			result.crewId(),
			result.hostUserId(),
			result.themeName(),
			result.place(),
			result.date(),
			result.time(),
			result.capacity(),
			result.totalCost(),
			result.reservationLink(),
			result.openChatLink(),
			result.description(),
			result.status(),
			result.result()
		);
	}
}
