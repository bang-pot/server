package com.banglog.home.presentation;

import com.banglog.home.domain.view.HomeMyCrewsView;
import com.banglog.home.domain.view.HomePublicCrewPreviewView;
import com.banglog.home.domain.view.HomeThemePreviewView;
import com.banglog.home.domain.view.HomeUpcomingMeetingsView;
import com.banglog.home.domain.view.HomeView;

final class HomeDtoMapper {

	private HomeDtoMapper() {
	}

	static HomeDto.HomeResponse toResponse(HomeView result) {
		return new HomeDto.HomeResponse(
			result.isLoggedIn(),
			new HomeDto.MyCrewsSectionResponse(
				result.myCrews().items().stream()
					.map(HomeDtoMapper::toMyCrewItemResponse)
					.toList(),
				result.myCrews().totalCount()
			),
			new HomeDto.UpcomingMeetingsSectionResponse(
				toUpcomingMeetingItemResponse(result.upcomingMeetings().nearestMeeting()),
				result.upcomingMeetings().totalCount()
			),
			new HomeDto.ActivityRecordSectionResponse(
				result.activityRecord().completedCount(),
				result.activityRecord().successRate()
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
		if (item == null) {
			return null;
		}
		return new HomeDto.UpcomingMeetingItemResponse(
			item.meetingId(),
			item.themeName(),
			item.date(),
			item.time()
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
