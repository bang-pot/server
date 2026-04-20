package com.bangpot.home.presentation;

import java.util.List;

final class HomeDto {

	record HomeResponse(
		boolean isLoggedIn,
		CtaResponse cta,
		MyCrewsSectionResponse myCrews,
		UpcomingMeetingsSectionResponse upcomingMeetings,
		PublicCrewPreviewSectionResponse publicCrewPreview,
		ThemeExplorePreviewSectionResponse themeExplorePreview
	) {
	}

	record CtaResponse(
		boolean canCreateCrew,
		boolean canExplorePublicCrews
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
		List<UpcomingMeetingItemResponse> items,
		Long totalCount
	) {
	}

	record UpcomingMeetingItemResponse(
		Long meetingId,
		String title,
		Long crewId,
		String crewName,
		String date,
		String time,
		String status
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
		Long memberCount,
		boolean isPublic
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
