package com.bangpot.home.application.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.bangpot.common.cache.CacheNames;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.view.ThemePreviewView;
import com.bangpot.home.domain.view.HomeActivityRecordView;
import com.bangpot.home.domain.view.HomeMyCrewsView;
import com.bangpot.home.domain.view.HomePublicCrewPreviewView;
import com.bangpot.home.domain.view.HomeThemePreviewView;
import com.bangpot.home.domain.view.HomeUpcomingMeetingsView;
import com.bangpot.home.domain.view.HomeView;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AnonymousHomeReader {

	private final CrewQueryRepository crewQueryRepository;
	private final ExploreQueryRepository exploreQueryRepository;

	@Cacheable(
		cacheNames = CacheNames.HOME_ANONYMOUS,
		key = "'v1:publicCrewLimit=' + #publicCrewPreviewLimit + ':themeLimit=' + #themePreviewLimit"
	)
	public HomeView read(int publicCrewPreviewLimit, int themePreviewLimit) {
		return HomeView.of(
			false,
			HomeMyCrewsView.of(List.of(), 0L),
			HomeUpcomingMeetingsView.empty(),
			HomeActivityRecordView.empty(),
			loadPublicCrewPreviewSection(publicCrewPreviewLimit),
			loadThemeExplorePreviewSection(themePreviewLimit)
		);
	}

	private HomePublicCrewPreviewView loadPublicCrewPreviewSection(int limit) {
		PublicCrewPreviewView result = crewQueryRepository.findPublicCrewPreviewView(limit);
		return HomePublicCrewPreviewView.of(
			result.items().stream()
				.map(item -> HomePublicCrewPreviewView.Item.of(
					item.crewId(),
					item.crewName(),
					item.coverImageUrl(),
					item.memberCount()
				))
				.toList()
		);
	}

	private HomeThemePreviewView loadThemeExplorePreviewSection(int limit) {
		ThemePreviewView result = exploreQueryRepository.findThemePreviewView(null, limit);
		return HomeThemePreviewView.of(
			result.items().stream()
				.map(item -> HomeThemePreviewView.Item.of(
					item.themeId(),
					item.themeName(),
					item.storeName(),
					item.regionLabel(),
					item.posterImageUrl(),
					item.favoriteCount(),
					item.isFavorite()
				))
				.toList()
		);
	}
}
