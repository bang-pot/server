package com.banglog.meeting.presentation;

import com.banglog.meeting.application.usecase.CreateMeetingUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.banglog.meeting.application.usecase.CancelMeetingUseCase;
import com.banglog.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.CompleteMeetingUseCase;
import com.banglog.meeting.application.usecase.GetMeetingDetailUseCase;
import com.banglog.meeting.application.usecase.GetMeetingsUseCase;
import com.banglog.meeting.application.usecase.JoinMeetingUseCase;
import com.banglog.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.banglog.meeting.application.usecase.UpdateMeetingUseCase;
import com.banglog.meeting.domain.view.MeetingDetailView;
import com.banglog.meeting.domain.view.MeetingsView;

final class MeetingDtoMapper {

	private MeetingDtoMapper() {
	}

	static CreateMeetingUseCase.Command toCommand(Long crewId, Long userId, MeetingDto.CreateMeetingRequest request) {
		return CreateMeetingUseCase.Command.of(
			crewId,
			userId,
			request.title(),
			request.date(),
			request.time(),
			request.place(),
			request.themeName(),
			request.capacity(),
			request.totalCost(),
			request.contactLink(),
			request.description()
		);
	}

	static MeetingDto.CreateMeetingResponse toResponse(CreateMeetingUseCase.Result result) {
		return new MeetingDto.CreateMeetingResponse(
			result.meetingId(),
			result.crewId(),
			result.title(),
			result.themeName(),
			result.place(),
			result.date(),
			result.time(),
			result.status()
		);
	}

	static UpdateMeetingUseCase.Command toCommand(
		Long crewId,
		Long meetingId,
		Long userId,
		MeetingDto.UpdateMeetingRequest request
	) {
		return UpdateMeetingUseCase.Command.of(
			crewId,
			meetingId,
			userId,
			request.title(),
			request.date(),
			request.time(),
			request.place(),
			request.themeName(),
			request.capacity(),
			request.totalCost(),
			request.contactLink(),
			request.description()
		);
	}

	static MeetingDto.UpdateMeetingResponse toResponse(UpdateMeetingUseCase.Result result) {
		return new MeetingDto.UpdateMeetingResponse(
			result.meetingId(),
			result.crewId(),
			result.hostUserId(),
			result.title(),
			result.themeName(),
			result.place(),
			result.date(),
			result.time(),
			result.capacity(),
			result.totalCost(),
			result.contactLink(),
			result.description(),
			result.status()
		);
	}

	static GetMeetingsUseCase.Query toQuery(Long crewId, Long userId, int page, int size) {
		return GetMeetingsUseCase.Query.of(crewId, userId, page, size);
	}

	static MeetingDto.MeetingListPageResponse toListResponse(MeetingsView meetingsView) {
		return new MeetingDto.MeetingListPageResponse(
			meetingsView.items().stream()
				.map(item -> new MeetingDto.MeetingListResponse(
					item.meetingId(),
					item.title(),
					item.themeName(),
					item.place(),
					item.date(),
					item.time(),
					item.status(),
					item.participantCount(),
					item.capacity()
				))
				.toList(),
			new MeetingDto.PageInfoResponse(
				meetingsView.page().page(),
				meetingsView.page().size(),
				meetingsView.page().hasNext()
			)
		);
	}

	static GetMeetingDetailUseCase.Query toQuery(Long crewId, Long meetingId, Long userId) {
		return GetMeetingDetailUseCase.Query.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingDetailResponse toResponse(MeetingDetailView result) {
		return new MeetingDto.MeetingDetailResponse(
			result.meetingId(),
			result.crewId(),
			result.hostUserId(),
			result.title(),
			result.themeName(),
			result.place(),
			result.date(),
			result.time(),
			result.capacity(),
			result.participantCount(),
			result.totalCost(),
			result.contactLink(),
			result.description(),
			result.status(),
			result.myParticipationStatus()
		);
	}

	static JoinMeetingUseCase.Command toCommand(Long crewId, Long meetingId, Long userId) {
		return JoinMeetingUseCase.Command.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingJoinResponse toResponse(JoinMeetingUseCase.Result result) {
		return new MeetingDto.MeetingJoinResponse(result.meetingId(), result.myParticipationStatus());
	}

	static CancelMeetingParticipationUseCase.Command toCancelCommand(Long crewId, Long meetingId, Long userId) {
		return CancelMeetingParticipationUseCase.Command.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingJoinResponse toResponse(CancelMeetingParticipationUseCase.Result result) {
		return new MeetingDto.MeetingJoinResponse(result.meetingId(), result.myParticipationStatus());
	}

	static CloseMeetingRecruitmentUseCase.Command toCloseRecruitmentCommand(Long crewId, Long meetingId, Long userId) {
		return CloseMeetingRecruitmentUseCase.Command.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingStatusChangeResponse toResponse(CloseMeetingRecruitmentUseCase.Result result) {
		return new MeetingDto.MeetingStatusChangeResponse(result.meetingId(), result.status());
	}

	static ReopenMeetingRecruitmentUseCase.Command toReopenRecruitmentCommand(Long crewId, Long meetingId, Long userId) {
		return ReopenMeetingRecruitmentUseCase.Command.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingStatusChangeResponse toResponse(ReopenMeetingRecruitmentUseCase.Result result) {
		return new MeetingDto.MeetingStatusChangeResponse(result.meetingId(), result.status());
	}

	static CancelMeetingUseCase.Command toCancelMeetingCommand(Long crewId, Long meetingId, Long userId) {
		return CancelMeetingUseCase.Command.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingStatusChangeResponse toResponse(CancelMeetingUseCase.Result result) {
		return new MeetingDto.MeetingStatusChangeResponse(result.meetingId(), result.status());
	}

	static CompleteMeetingUseCase.Command toCompleteMeetingCommand(Long crewId, Long meetingId, Long userId) {
		return CompleteMeetingUseCase.Command.of(crewId, meetingId, userId);
	}

	static MeetingDto.MeetingStatusChangeResponse toResponse(CompleteMeetingUseCase.Result result) {
		return new MeetingDto.MeetingStatusChangeResponse(result.meetingId(), result.status());
	}

}
