package com.bangpot.meeting.presentation;

import java.util.List;

import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;

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
			toPhotoInputs(request.photos())
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
			toUpdatePhotoInputs(request.photos())
		);
	}

	static DeleteMeetingLogUseCase.Command toCommand(Long logId, Long userId) {
		return DeleteMeetingLogUseCase.Command.of(logId, userId);
	}

	static GetMyMeetingLogUseCase.Query toQuery(Long meetingId, Long userId) {
		return GetMyMeetingLogUseCase.Query.of(meetingId, userId);
	}

	static GetMeetingLogDetailUseCase.Query toDetailQuery(Long logId, Long userId) {
		return GetMeetingLogDetailUseCase.Query.of(logId, userId);
	}

	static MeetingLogDto.MeetingLogWriteResponse toResponse(CreateMeetingLogUseCase.Result result) {
		return new MeetingLogDto.MeetingLogWriteResponse(result.logId(), result.meetingId());
	}

	static MeetingLogDto.MeetingLogWriteResponse toResponse(UpdateMeetingLogUseCase.Result result) {
		return new MeetingLogDto.MeetingLogWriteResponse(result.logId(), result.meetingId());
	}

	static MeetingLogDto.MeetingLogDeleteResponse toResponse(DeleteMeetingLogUseCase.Result result) {
		return new MeetingLogDto.MeetingLogDeleteResponse(result.logId());
	}

	static MeetingLogDto.MeetingLogPhotoUploadResponse toResponse(UploadMeetingLogPhotoUseCase.Result result) {
		return new MeetingLogDto.MeetingLogPhotoUploadResponse(result.url(), result.sizeBytes());
	}

	static MeetingLogDto.MeetingLogDetailResponse toResponse(GetMyMeetingLogUseCase.Result result) {
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
			result.body(),
			result.photos()
		);
	}

	static MeetingLogDto.MeetingLogDetailResponse toResponse(GetMeetingLogDetailUseCase.Result result) {
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
			result.body(),
			result.photos()
		);
	}

	private static List<CreateMeetingLogUseCase.PhotoInput> toPhotoInputs(List<MeetingLogDto.PhotoRequest> photos) {
		if (photos == null) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> CreateMeetingLogUseCase.PhotoInput.of(photo.url(), photo.sizeBytes()))
			.toList();
	}

	private static List<UpdateMeetingLogUseCase.PhotoInput> toUpdatePhotoInputs(List<MeetingLogDto.PhotoRequest> photos) {
		if (photos == null) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> UpdateMeetingLogUseCase.PhotoInput.of(photo.url(), photo.sizeBytes()))
			.toList();
	}
}

