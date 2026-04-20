package com.bangpot.home.presentation;

import com.bangpot.home.application.usecase.GetHomeUseCase;

final class HomeDtoMapper {

	private HomeDtoMapper() {
	}

	static HomeDto.HomeResponse toResponse(GetHomeUseCase.Result result) {
		return new HomeDto.HomeResponse(
			result.isLoggedIn(),
			new HomeDto.CtaResponse(
				result.cta().canCreateCrew(),
				result.cta().canExplorePublicCrews()
			),
			new HomeDto.MyCrewsSectionResponse(
				result.myCrews().items().stream()
					.map(item -> new HomeDto.MyCrewItemResponse(item.crewId(), item.crewName()))
					.toList(),
				result.myCrews().totalCount()
			),
			new HomeDto.UpcomingMeetingsSectionResponse(
				result.upcomingMeetings().items().stream()
					.map(item -> new HomeDto.UpcomingMeetingItemResponse(
						item.meetingId(),
						item.title(),
						item.crewId(),
						item.crewName(),
						item.date(),
						item.time(),
						item.status()
					))
					.toList(),
				result.upcomingMeetings().totalCount()
			),
			new HomeDto.PublicCrewPreviewSectionResponse(
				result.publicCrewPreview().items().stream()
					.map(item -> new HomeDto.PublicCrewPreviewItemResponse(
						item.crewId(),
						item.crewName(),
						item.coverImageUrl(),
						item.memberCount(),
						item.isPublic()
					))
					.toList()
			),
			new HomeDto.ThemeExplorePreviewSectionResponse(
				result.themeExplorePreview().items().stream()
					.map(item -> new HomeDto.ThemeExplorePreviewItemResponse(
						item.themeId(),
						item.themeName(),
						item.storeName(),
						item.regionName(),
						item.thumbnailUrl(),
						item.favoriteCount(),
						item.isFavorite()
					))
					.toList()
			)
		);
	}
}
