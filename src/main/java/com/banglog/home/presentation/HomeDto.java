package com.banglog.home.presentation;

import java.util.List;

final class HomeDto {

	record HomeResponse(
		boolean isLoggedIn,
		MyCrewsSectionResponse myCrews,
		UpcomingMeetingsSectionResponse upcomingMeetings,
		ActivityRecordSectionResponse activityRecord,
		PublicCrewPreviewSectionResponse publicCrewPreview,
		ThemeExplorePreviewSectionResponse themeExplorePreview
	) {
	}

	record MyCrewsSectionResponse(
		List<MyCrewItemResponse> items,
		Long totalCount
	) {
	}

	record MyCrewItemResponse(
		Long crewId,
		String crewName
	) {
	}

	record UpcomingMeetingsSectionResponse(
		UpcomingMeetingItemResponse nearestMeeting,
		Long totalCount
	) {
	}

	record UpcomingMeetingItemResponse(
		Long meetingId,
		String themeName,
		String date,
		String time
	) {
	}

	record ActivityRecordSectionResponse(
		Long completedCount,
		Integer successRate
	) {
	}

	record PublicCrewPreviewSectionResponse(
		List<PublicCrewPreviewItemResponse> items
	) {
	}

	record PublicCrewPreviewItemResponse(
		Long crewId,
		String crewName,
		String coverImageUrl,
		Long memberCount
	) {
	}

	record ThemeExplorePreviewSectionResponse(
		List<ThemeExplorePreviewItemResponse> items
	) {
	}

	record ThemeExplorePreviewItemResponse(
		Long themeId,
		String themeName,
		String storeName,
		String regionName,
		String thumbnailUrl,
		Integer favoriteCount,
		boolean isFavorite
	) {
	}

	private HomeDto() {
	}
}
