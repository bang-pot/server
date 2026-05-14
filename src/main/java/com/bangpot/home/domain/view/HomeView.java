package com.bangpot.home.domain.view;

public record HomeView(
	boolean isLoggedIn,
	HomeMyCrewsView myCrews,
	HomeUpcomingMeetingsView upcomingMeetings,
	HomeActivityRecordView activityRecord,
	HomePublicCrewPreviewView publicCrewPreview,
	HomeThemePreviewView themeExplorePreview
) {
	public static HomeView of(
		boolean isLoggedIn,
		HomeMyCrewsView myCrews,
		HomeUpcomingMeetingsView upcomingMeetings,
		HomeActivityRecordView activityRecord,
		HomePublicCrewPreviewView publicCrewPreview,
		HomeThemePreviewView themeExplorePreview
	) {
		return new HomeView(isLoggedIn, myCrews, upcomingMeetings, activityRecord, publicCrewPreview, themeExplorePreview);
	}
}
