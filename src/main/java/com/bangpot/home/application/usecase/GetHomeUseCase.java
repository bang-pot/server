package com.bangpot.home.application.usecase;

import java.util.List;

public interface GetHomeUseCase {

	Result handle(Query query);

	record Query(Long userId) {
		public static Query of(Long userId) {
			return new Query(userId);
		}
	}

	record Result(
		boolean isLoggedIn,
		Cta cta,
		MyCrewsSection myCrews,
		UpcomingMeetingsSection upcomingMeetings,
		PublicCrewPreviewSection publicCrewPreview,
		ThemeExplorePreviewSection themeExplorePreview
	) {
		public static Result of(
			boolean isLoggedIn,
			Cta cta,
			MyCrewsSection myCrews,
			UpcomingMeetingsSection upcomingMeetings,
			PublicCrewPreviewSection publicCrewPreview,
			ThemeExplorePreviewSection themeExplorePreview
		) {
			return new Result(isLoggedIn, cta, myCrews, upcomingMeetings, publicCrewPreview, themeExplorePreview);
		}
	}

	record Cta(
		boolean canCreateCrew,
		boolean canExplorePublicCrews
	) {
		public static Cta of(boolean canCreateCrew, boolean canExplorePublicCrews) {
			return new Cta(canCreateCrew, canExplorePublicCrews);
		}
	}

	record MyCrewsSection(
		List<MyCrewItem> items,
		Long totalCount
	) {
		public static MyCrewsSection of(List<MyCrewItem> items, Long totalCount) {
			return new MyCrewsSection(items, totalCount);
		}
	}

	record MyCrewItem(
		Long crewId,
		String crewName
	) {
		public static MyCrewItem of(Long crewId, String crewName) {
			return new MyCrewItem(crewId, crewName);
		}
	}

	record UpcomingMeetingsSection(
		List<UpcomingMeetingItem> items,
		Long totalCount
	) {
		public static UpcomingMeetingsSection of(List<UpcomingMeetingItem> items, Long totalCount) {
			return new UpcomingMeetingsSection(items, totalCount);
		}
	}

	record UpcomingMeetingItem(
		Long meetingId,
		String title,
		Long crewId,
		String crewName,
		String date,
		String time,
		String status
	) {
		public static UpcomingMeetingItem of(
			Long meetingId,
			String title,
			Long crewId,
			String crewName,
			String date,
			String time,
			String status
		) {
			return new UpcomingMeetingItem(meetingId, title, crewId, crewName, date, time, status);
		}
	}

	record PublicCrewPreviewSection(List<PublicCrewPreviewItem> items) {
		public static PublicCrewPreviewSection of(List<PublicCrewPreviewItem> items) {
			return new PublicCrewPreviewSection(items);
		}
	}

	record PublicCrewPreviewItem(
		Long crewId,
		String crewName,
		String coverImageUrl,
		Long memberCount,
		boolean isPublic
	) {
		public static PublicCrewPreviewItem of(
			Long crewId,
			String crewName,
			String coverImageUrl,
			Long memberCount,
			boolean isPublic
		) {
			return new PublicCrewPreviewItem(crewId, crewName, coverImageUrl, memberCount, isPublic);
		}
	}

	record ThemeExplorePreviewSection(List<ThemeExplorePreviewItem> items) {
		public static ThemeExplorePreviewSection of(List<ThemeExplorePreviewItem> items) {
			return new ThemeExplorePreviewSection(items);
		}
	}

	record ThemeExplorePreviewItem(
		Long themeId,
		String themeName,
		String storeName,
		String regionName,
		String thumbnailUrl
	) {
		public static ThemeExplorePreviewItem of(
			Long themeId,
			String themeName,
			String storeName,
			String regionName,
			String thumbnailUrl
		) {
			return new ThemeExplorePreviewItem(themeId, themeName, storeName, regionName, thumbnailUrl);
		}
	}
}
