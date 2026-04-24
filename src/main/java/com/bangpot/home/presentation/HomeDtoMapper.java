package com.bangpot.home.presentation;

import com.bangpot.home.application.usecase.GetHomeUseCase;
import com.bangpot.home.domain.view.HomeMyCrewsView;
import com.bangpot.home.domain.view.HomePublicCrewPreviewView;
import com.bangpot.home.domain.view.HomeThemePreviewView;
import com.bangpot.home.domain.view.HomeUpcomingMeetingsView;

final class HomeDtoMapper {

	private HomeDtoMapper() {
	}

	static HomeDto.HomeResponse toResponse(GetHomeUseCase.Result result) {
		return new HomeDto.HomeResponse(
			result.isLoggedIn(),
			new HomeDto.MyCrewsSectionResponse(
				result.myCrews().items().stream()
					.map(HomeDtoMapper::toMyCrewItemResponse)
					.toList(),
				result.myCrews().totalCount()
			),
			new HomeDto.UpcomingMeetingsSectionResponse(
				result.upcomingMeetings().items().stream()
					.map(HomeDtoMapper::toUpcomingMeetingItemResponse)
					.toList(),
				result.upcomingMeetings().totalCount()
			),
			new HomeDto.PublicCrewPreviewSectionResponse(
				result.publicCrewPreview().items().stream()
					.map(HomeDtoMapper::toPublicCrewPreviewItemResponse)
					.toList()
			),
			new HomeDto.ThemeExplorePreviewSectionResponse(
				result.themeExplorePreview().items().stream()
					.map(HomeDtoMapper::toThemePreviewItemResponse)
					.toList()
			)
		);
	}

	private static HomeDto.MyCrewItemResponse toMyCrewItemResponse(HomeMyCrewsView.Item item) {
		return new HomeDto.MyCrewItemResponse(item.crewId(), item.crewName());
	}

	private static HomeDto.UpcomingMeetingItemResponse toUpcomingMeetingItemResponse(HomeUpcomingMeetingsView.Item item) {
		return new HomeDto.UpcomingMeetingItemResponse(
			item.meetingId(),
			item.title(),
			item.crewId(),
			item.crewName(),
			item.date(),
			item.time(),
			item.status()
		);
	}

	private static HomeDto.PublicCrewPreviewItemResponse toPublicCrewPreviewItemResponse(HomePublicCrewPreviewView.Item item) {
		return new HomeDto.PublicCrewPreviewItemResponse(
			item.crewId(),
			item.crewName(),
			item.coverImageUrl(),
			item.memberCount()
		);
	}

	private static HomeDto.ThemeExplorePreviewItemResponse toThemePreviewItemResponse(HomeThemePreviewView.Item item) {
		return new HomeDto.ThemeExplorePreviewItemResponse(
			item.themeId(),
			item.themeName(),
			item.storeName(),
			item.regionName(),
			item.thumbnailUrl(),
			item.favoriteCount(),
			item.isFavorite()
		);
	}
}
