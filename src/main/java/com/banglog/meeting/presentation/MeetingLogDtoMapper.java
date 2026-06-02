package com.banglog.meeting.presentation;

import java.util.List;

import com.banglog.meeting.application.usecase.CreateMeetingLogUseCase;
import com.banglog.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.banglog.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.banglog.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.banglog.meeting.application.usecase.UpdateMeetingLogUseCase;
import com.banglog.meeting.domain.view.MeetingLogDetailView;
import com.banglog.meeting.domain.view.MyMeetingLogView;

final class MeetingLogDtoMapper {

	private MeetingLogDtoMapper() {
	}

	static CreateMeetingLogUseCase.Command toCommand(
		Long meetingId,
		Long userId,
		MeetingLogDto.CreateMeetingLogRequest request
	) {
		return CreateMeetingLogUseCase.Command.of(
			meetingId,
			userId,
			request.body(),
			toPhotoInputs(request.photos()),
			request.result()
		);
	}

	static UpdateMeetingLogUseCase.Command toCommand(
		Long logId,
		Long userId,
		MeetingLogDto.UpdateMeetingLogRequest request
	) {
		return UpdateMeetingLogUseCase.Command.of(
			logId,
			userId,
			request.body(),
			toUpdatePhotoInputs(request.photos()),
			request.result()
		);
	}

	static DeleteMeetingLogUseCase.Command toDeleteCommand(
		Long crewId,
		Long logId,
		Long userId,
		MeetingLogDto.DeleteMeetingLogRequest request
	) {
		return DeleteMeetingLogUseCase.Command.of(
			crewId,
			logId,
			userId,
			request == null ? null : request.deleteReason()
		);
	}

	static GetMyMeetingLogUseCase.Query toQuery(Long meetingId, Long userId) {
		return GetMyMeetingLogUseCase.Query.of(meetingId, userId);
	}

	static GetMeetingLogDetailUseCase.Query toDetailQuery(Long crewId, Long logId, Long userId) {
		return GetMeetingLogDetailUseCase.Query.of(crewId, logId, userId);
	}

	static MeetingLogDto.MeetingLogWriteResponse toResponse(CreateMeetingLogUseCase.Result result) {
		return new MeetingLogDto.MeetingLogWriteResponse(result.logId(), result.meetingId());
	}

	static MeetingLogDto.MeetingLogWriteResponse toResponse(UpdateMeetingLogUseCase.Result result) {
		return new MeetingLogDto.MeetingLogWriteResponse(result.logId(), result.meetingId());
	}

	static MeetingLogDto.MeetingLogDeleteResponse toResponse(DeleteMeetingLogUseCase.Result result) {
		return new MeetingLogDto.MeetingLogDeleteResponse(result.logId(), result.deletedBy().name());
	}

	static MeetingLogDto.MyMeetingLogResponse toResponse(MyMeetingLogView result) {
		return new MeetingLogDto.MyMeetingLogResponse(
			result.status().name(),
			result.logId(),
			result.meetingId(),
			result.meetingTitle(),
			result.themeName(),
			result.place(),
			result.date(),
			result.authorNickname(),
			result.createdAt(),
			result.updatedAt(),
			result.result() == null ? null : result.result().name(),
			result.body(),
			result.photos()
		);
	}

	static MeetingLogDto.MeetingLogDetailResponse toResponse(MeetingLogDetailView result) {
		return new MeetingLogDto.MeetingLogDetailResponse(
			result.logId(),
			result.meetingId(),
			result.meetingTitle(),
			result.themeName(),
			result.place(),
			result.date(),
			result.authorNickname(),
			result.createdAt(),
			result.updatedAt(),
			result.result() == null ? null : result.result().name(),
			result.body(),
			result.photos()
		);
	}

	private static List<CreateMeetingLogUseCase.PhotoInput> toPhotoInputs(List<MeetingLogDto.PhotoRequest> photos) {
		if (photos == null) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> CreateMeetingLogUseCase.PhotoInput.of(photo.uploadId()))
			.toList();
	}

	private static List<UpdateMeetingLogUseCase.PhotoInput> toUpdatePhotoInputs(List<MeetingLogDto.PhotoRequest> photos) {
		if (photos == null) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> UpdateMeetingLogUseCase.PhotoInput.of(photo.uploadId()))
			.toList();
	}
}
