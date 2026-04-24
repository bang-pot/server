package com.bangpot.home.application.usecase;

import com.bangpot.home.domain.view.HomeMyCrewsView;
import com.bangpot.home.domain.view.HomePublicCrewPreviewView;
import com.bangpot.home.domain.view.HomeThemePreviewView;
import com.bangpot.home.domain.view.HomeUpcomingMeetingsView;

public interface GetHomeUseCase {

	Result handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record Result(
		boolean isLoggedIn,
		HomeMyCrewsView myCrews,
		HomeUpcomingMeetingsView upcomingMeetings,
		HomePublicCrewPreviewView publicCrewPreview,
		HomeThemePreviewView themeExplorePreview
	) {
		public static Result of(
			boolean isLoggedIn,
			HomeMyCrewsView myCrews,
			HomeUpcomingMeetingsView upcomingMeetings,
			HomePublicCrewPreviewView publicCrewPreview,
			HomeThemePreviewView themeExplorePreview
		) {
			return new Result(isLoggedIn, myCrews, upcomingMeetings, publicCrewPreview, themeExplorePreview);
		}
	}
}
