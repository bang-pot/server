package com.bangpot.home.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;
import com.bangpot.home.application.port.HomePublicCrewPreviewReadRepository;
import com.bangpot.home.application.port.HomeUpcomingMeetingReadRepository;
import com.bangpot.home.application.usecase.GetHomeUseCase;
import com.bangpot.user.application.port.MyCrewReadRepository;
import com.bangpot.user.application.port.ProfileHubReadRepository;
import com.bangpot.user.application.port.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetHomeService implements GetHomeUseCase {

	private static final int MY_CREWS_LIMIT = 5;
	private static final int UPCOMING_MEETINGS_LIMIT = 5;
	private static final int PUBLIC_CREW_PREVIEW_LIMIT = 8;
	private static final int THEME_PREVIEW_LIMIT = 8;

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final ProfileHubReadRepository profileHubReadRepository;
	private final MyCrewReadRepository myCrewReadRepository;
	private final HomeUpcomingMeetingReadRepository homeUpcomingMeetingReadRepository;
	private final HomePublicCrewPreviewReadRepository homePublicCrewPreviewReadRepository;
	private final GetExploreThemesUseCase getExploreThemesUseCase;
	private final Clock clock;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		boolean isLoggedIn = isCompletedLoggedInUser(query.userId());

		return Result.of(
			isLoggedIn,
			Cta.of(isLoggedIn, true),
			loadMyCrewsSection(query.userId(), isLoggedIn),
			loadUpcomingMeetingsSection(query.userId(), isLoggedIn),
			loadPublicCrewPreviewSection(),
			loadThemeExplorePreviewSection(query.userId(), isLoggedIn)
		);
	}

	private boolean isCompletedLoggedInUser(Long userId) {
		if (userId == null) {
			return false;
		}

		return authUserRepository.findById(userId)
			.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL)
			.flatMap(ignored -> userRepository.findById(userId))
			.isPresent();
	}

	private MyCrewsSection loadMyCrewsSection(Long userId, boolean isLoggedIn) {
		if (!isLoggedIn) {
			return MyCrewsSection.of(List.of(), 0L);
		}

		MyCrewReadRepository.SearchResult result = myCrewReadRepository.search(userId, 0, MY_CREWS_LIMIT);
		long totalCount = profileHubReadRepository.loadCounts(userId).myCrewsCount();

		return MyCrewsSection.of(
			result.items().stream()
				.map(item -> MyCrewItem.of(item.crewId(), item.crewName()))
				.toList(),
			totalCount
		);
	}

	private UpcomingMeetingsSection loadUpcomingMeetingsSection(Long userId, boolean isLoggedIn) {
		if (!isLoggedIn) {
			return UpcomingMeetingsSection.of(List.of(), 0L);
		}

		LocalDateTime now = LocalDateTime.now(clock);
		HomeUpcomingMeetingReadRepository.Result result = homeUpcomingMeetingReadRepository.findUpcomingMeetings(
			userId,
			UPCOMING_MEETINGS_LIMIT,
			now.toLocalDate().toString(),
			now.toLocalTime().withSecond(0).withNano(0).toString()
		);

		return UpcomingMeetingsSection.of(
			result.items().stream()
				.map(item -> UpcomingMeetingItem.of(
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

	private PublicCrewPreviewSection loadPublicCrewPreviewSection() {
		return PublicCrewPreviewSection.of(
			homePublicCrewPreviewReadRepository.findPreviewItems(PUBLIC_CREW_PREVIEW_LIMIT).stream()
				.map(item -> PublicCrewPreviewItem.of(
					item.crewId(),
					item.crewName(),
					item.coverImageUrl(),
					item.memberCount(),
					item.isPublic()
				))
				.toList()
		);
	}

	private ThemeExplorePreviewSection loadThemeExplorePreviewSection(Long userId, boolean isLoggedIn) {
		GetExploreThemesUseCase.Result result = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of(
				isLoggedIn ? userId : null,
				null,
				null,
				null,
				null,
				0,
				THEME_PREVIEW_LIMIT
			)
		);

		return ThemeExplorePreviewSection.of(
			result.items().stream()
				.map(item -> ThemeExplorePreviewItem.of(
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
