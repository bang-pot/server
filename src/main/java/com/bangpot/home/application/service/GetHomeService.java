package com.bangpot.home.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.view.ThemePreviewView;
import com.bangpot.home.domain.view.HomeMyCrewsView;
import com.bangpot.home.domain.view.HomePublicCrewPreviewView;
import com.bangpot.home.domain.view.HomeThemePreviewView;
import com.bangpot.home.domain.view.HomeUpcomingMeetingsView;
import com.bangpot.home.domain.view.HomeView;
import com.bangpot.home.application.usecase.GetHomeUseCase;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;
import com.bangpot.user.application.port.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetHomeService implements GetHomeUseCase {

	private static final int MY_CREWS_LIMIT = 5;
	private static final int UPCOMING_MEETINGS_LIMIT = 5;
	private static final int PUBLIC_CREW_PREVIEW_LIMIT = 8;
	private static final int THEME_PREVIEW_LIMIT = 8;

	private final UserQueryRepository userQueryRepository;
	private final CrewQueryRepository crewQueryRepository;
	private final MeetingQueryRepository meetingQueryRepository;
	private final ExploreQueryRepository exploreQueryRepository;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public HomeView handle(Query query) {
		boolean isLoggedIn = isCompletedLoggedInUser(query.userId());

		return HomeView.of(
			isLoggedIn,
			loadMyCrewsSection(query.userId(), isLoggedIn),
			loadUpcomingMeetingsSection(query.userId(), isLoggedIn),
			loadPublicCrewPreviewSection(),
			loadThemeExplorePreviewSection(query.userId(), isLoggedIn)
		);
	}

	private boolean isCompletedLoggedInUser(Long userId) {
		return userId != null && userQueryRepository.existsCompletedUser(userId);
	}

	private HomeMyCrewsView loadMyCrewsSection(Long userId, boolean isLoggedIn) {
		if (!isLoggedIn) {
			return HomeMyCrewsView.of(List.of(), 0L);
		}

		MyCrewsView result = crewQueryRepository.findMyCrewsViewByMemberUserId(userId, 0, MY_CREWS_LIMIT);
		long totalCount = crewQueryRepository.countMyCrewsViewByMemberUserId(userId);

		return HomeMyCrewsView.of(
			result.items().stream()
				.map(item -> HomeMyCrewsView.Item.of(item.crewId(), item.crewName()))
				.toList(),
			totalCount
		);
	}

	private HomeUpcomingMeetingsView loadUpcomingMeetingsSection(Long userId, boolean isLoggedIn) {
		if (!isLoggedIn) {
			return HomeUpcomingMeetingsView.of(List.of(), 0L);
		}

		LocalDateTime now = LocalDateTime.now(clock);
		UpcomingMeetingsView result = meetingQueryRepository.findUpcomingMeetingsViewByUserId(
			userId,
			UPCOMING_MEETINGS_LIMIT,
			now.toLocalDate().toString(),
			now.toLocalTime().withSecond(0).withNano(0).toString()
		);

		return HomeUpcomingMeetingsView.of(
			result.items().stream()
				.map(item -> HomeUpcomingMeetingsView.Item.of(
					item.meetingId(),
					item.title(),
					item.crewId(),
					item.crewName(),
					item.date(),
					item.time(),
					item.status()
				))
				.toList(),
			result.totalCount()
		);
	}

	private HomePublicCrewPreviewView loadPublicCrewPreviewSection() {
		PublicCrewPreviewView result = crewQueryRepository.findPublicCrewPreviewView(PUBLIC_CREW_PREVIEW_LIMIT);
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

	private HomeThemePreviewView loadThemeExplorePreviewSection(Long userId, boolean isLoggedIn) {
		ThemePreviewView result = exploreQueryRepository.findThemePreviewView(isLoggedIn ? userId : null, THEME_PREVIEW_LIMIT);
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
