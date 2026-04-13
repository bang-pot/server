package com.bangpot.meeting.presentation;

import java.util.List;

import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.bangpot.meeting.application.usecase.CompleteMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.application.usecase.RecordMeetingResultUseCase;
import com.bangpot.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;

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
			result.result(),
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

	static RecordMeetingResultUseCase.Command toRecordResultCommand(
		Long crewId,
		Long meetingId,
		Long userId,
		MeetingDto.RecordMeetingResultRequest request
	) {
		return RecordMeetingResultUseCase.Command.of(crewId, meetingId, userId, request.result());
	}

	static MeetingDto.MeetingResultRecordResponse toResponse(RecordMeetingResultUseCase.Result result) {
		return new MeetingDto.MeetingResultRecordResponse(result.meetingId(), result.result());
	}
}
