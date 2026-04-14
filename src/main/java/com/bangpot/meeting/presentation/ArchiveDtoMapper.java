package com.bangpot.meeting.presentation;

import com.bangpot.meeting.application.usecase.GetArchivedMeetingsUseCase;

final class ArchiveDtoMapper {

	private ArchiveDtoMapper() {
	}

	static GetArchivedMeetingsUseCase.Query toQuery(Long userId, int page, int size) {
		return GetArchivedMeetingsUseCase.Query.of(userId, page, size);
	}

	static ArchiveDto.ArchiveMeetingsResponse toResponse(GetArchivedMeetingsUseCase.Result result) {
		return new ArchiveDto.ArchiveMeetingsResponse(
			result.items().stream()
				.map(item -> new ArchiveDto.ArchiveMeetingResponse(
					item.meetingId(),
					item.crewId(),
					item.crewName(),
					item.themeName(),
					item.place(),
					item.date(),
					item.result(),
					item.posterImageUrl()
				))
				.toList(),
			new ArchiveDto.ArchivePageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}
}

