package com.bangpot.gallerylog.presentation;

import java.util.List;

import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.gallerylog.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.gallerylog.application.usecase.UpdateMeetingLogUseCase;

final class GalleryLogDtoMapper {

	private GalleryLogDtoMapper() {
	}

	static CreateMeetingLogUseCase.Command toCommand(
		Long meetingId,
		Long userId,
		GalleryLogDto.CreateMeetingLogRequest request
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
		GalleryLogDto.UpdateMeetingLogRequest request
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

	static GalleryLogDto.MeetingLogWriteResponse toResponse(CreateMeetingLogUseCase.Result result) {
		return new GalleryLogDto.MeetingLogWriteResponse(result.logId(), result.meetingId());
	}

	static GalleryLogDto.MeetingLogWriteResponse toResponse(UpdateMeetingLogUseCase.Result result) {
		return new GalleryLogDto.MeetingLogWriteResponse(result.logId(), result.meetingId());
	}

	static GalleryLogDto.MeetingLogDeleteResponse toResponse(DeleteMeetingLogUseCase.Result result) {
		return new GalleryLogDto.MeetingLogDeleteResponse(result.logId());
	}

	static GalleryLogDto.MeetingLogDetailResponse toResponse(GetMyMeetingLogUseCase.Result result) {
		return new GalleryLogDto.MeetingLogDetailResponse(
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

	static GalleryLogDto.MeetingLogDetailResponse toResponse(GetMeetingLogDetailUseCase.Result result) {
		return new GalleryLogDto.MeetingLogDetailResponse(
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

	private static List<CreateMeetingLogUseCase.PhotoInput> toPhotoInputs(List<GalleryLogDto.PhotoRequest> photos) {
		if (photos == null) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> CreateMeetingLogUseCase.PhotoInput.of(photo.url(), photo.sizeBytes()))
			.toList();
	}

	private static List<UpdateMeetingLogUseCase.PhotoInput> toUpdatePhotoInputs(List<GalleryLogDto.PhotoRequest> photos) {
		if (photos == null) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> UpdateMeetingLogUseCase.PhotoInput.of(photo.url(), photo.sizeBytes()))
			.toList();
	}
}
