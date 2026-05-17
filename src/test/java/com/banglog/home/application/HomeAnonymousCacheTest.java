package com.banglog.home.application;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.banglog.common.cache.CacheNames;
import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.crew.domain.view.PublicCrewPreviewView;
import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.domain.view.ThemePreviewView;
import com.banglog.home.application.service.AnonymousHomeReader;
import com.banglog.home.application.service.GetHomeService;
import com.banglog.home.application.usecase.GetHomeUseCase;
import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.domain.view.MeetingActivityRecordView;
import com.banglog.meeting.domain.view.UpcomingMeetingsView;
import com.banglog.user.application.port.UserQueryRepository;

@SpringJUnitConfig(classes = HomeAnonymousCacheTest.TestConfig.class)
class HomeAnonymousCacheTest {

	private static final Instant BASE_TIME = Instant.parse("2026-04-19T00:00:00Z");
	private static final int PUBLIC_CREW_PREVIEW_LIMIT = 8;
	private static final int THEME_PREVIEW_LIMIT = 8;

	@Autowired
	private GetHomeUseCase getHomeUseCase;

	@Autowired
	private UserQueryRepository userQueryRepository;

	@Autowired
	private CrewQueryRepository crewQueryRepository;

	@Autowired
	private MeetingQueryRepository meetingQueryRepository;

	@Autowired
	private ExploreQueryRepository exploreQueryRepository;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		reset(userQueryRepository, crewQueryRepository, meetingQueryRepository, exploreQueryRepository);
		cacheManager.getCache(CacheNames.HOME_ANONYMOUS).clear();
		when(crewQueryRepository.findPublicCrewPreviewView(PUBLIC_CREW_PREVIEW_LIMIT))
			.thenReturn(publicCrewPreviewView());
		when(exploreQueryRepository.findThemePreviewView(null, THEME_PREVIEW_LIMIT))
			.thenReturn(themePreviewView(false));
	}

	@Test
	void reusesCachedAnonymousHome() {
		getHomeUseCase.handle(GetHomeUseCase.Query.of(null));
		getHomeUseCase.handle(GetHomeUseCase.Query.of(null));

		verify(crewQueryRepository, times(1)).findPublicCrewPreviewView(PUBLIC_CREW_PREVIEW_LIMIT);
		verify(exploreQueryRepository, times(1)).findThemePreviewView(null, THEME_PREVIEW_LIMIT);
		verify(userQueryRepository, never()).existsCompletedUser(Mockito.any());
		verify(meetingQueryRepository, never()).findUpcomingMeetingsViewByUserId(
			Mockito.any(),
			Mockito.anyInt(),
			anyString(),
			anyString()
		);
	}

	@Test
	void doesNotCacheLoggedInHome() {
		when(userQueryRepository.existsCompletedUser(7L)).thenReturn(true);
		when(crewQueryRepository.findMyCrewsViewByMemberUserId(7L, 0, 5))
			.thenReturn(MyCrewsView.of(List.of(), MyCrewsView.Page.of(0, 5, false)));
		when(crewQueryRepository.countMyCrewsViewByMemberUserId(7L)).thenReturn(0L);
		when(meetingQueryRepository.findUpcomingMeetingsViewByUserId(
			Mockito.eq(7L),
			Mockito.eq(1),
			Mockito.eq("2026-04-19"),
			Mockito.eq("00:00")
		)).thenReturn(UpcomingMeetingsView.of(List.of(), 0L));
		when(meetingQueryRepository.findActivityRecordViewByUserId(7L))
			.thenReturn(MeetingActivityRecordView.empty());
		when(exploreQueryRepository.findThemePreviewView(7L, THEME_PREVIEW_LIMIT))
			.thenReturn(themePreviewView(true));

		clearInvocations(crewQueryRepository, exploreQueryRepository);

		getHomeUseCase.handle(GetHomeUseCase.Query.of(7L));
		getHomeUseCase.handle(GetHomeUseCase.Query.of(7L));

		verify(crewQueryRepository, times(2)).findPublicCrewPreviewView(PUBLIC_CREW_PREVIEW_LIMIT);
		verify(exploreQueryRepository, times(2)).findThemePreviewView(7L, THEME_PREVIEW_LIMIT);
		verify(exploreQueryRepository, never()).findThemePreviewView(null, THEME_PREVIEW_LIMIT);
	}

	private static PublicCrewPreviewView publicCrewPreviewView() {
		return PublicCrewPreviewView.of(List.of(
			PublicCrewPreviewView.Item.of(31L, "Alpha Crew", null, 12L)
		));
	}

	private static ThemePreviewView themePreviewView(boolean isFavorite) {
		return ThemePreviewView.of(List.of(
			ThemePreviewView.Item.of(
				101L,
				"Deep Blue",
				"Room Escape",
				"Seoul Mapo",
				"https://cdn.example.com/theme.jpg",
				7,
				isFavorite
			)
		));
	}

	@Configuration
	@EnableCaching(proxyTargetClass = true)
	static class TestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheNames.HOME_ANONYMOUS);
		}

		@Bean
		UserQueryRepository userQueryRepository() {
			return Mockito.mock(UserQueryRepository.class);
		}

		@Bean
		CrewQueryRepository crewQueryRepository() {
			return Mockito.mock(CrewQueryRepository.class);
		}

		@Bean
		MeetingQueryRepository meetingQueryRepository() {
			return Mockito.mock(MeetingQueryRepository.class);
		}

		@Bean
		ExploreQueryRepository exploreQueryRepository() {
			return Mockito.mock(ExploreQueryRepository.class);
		}

		@Bean
		AnonymousHomeReader anonymousHomeReader(
			CrewQueryRepository crewQueryRepository,
			ExploreQueryRepository exploreQueryRepository
		) {
			return new AnonymousHomeReader(crewQueryRepository, exploreQueryRepository);
		}

		@Bean
		GetHomeUseCase getHomeUseCase(
			UserQueryRepository userQueryRepository,
			CrewQueryRepository crewQueryRepository,
			MeetingQueryRepository meetingQueryRepository,
			ExploreQueryRepository exploreQueryRepository,
			AnonymousHomeReader anonymousHomeReader
		) {
			return new GetHomeService(
				userQueryRepository,
				crewQueryRepository,
				meetingQueryRepository,
				exploreQueryRepository,
				anonymousHomeReader,
				Clock.fixed(BASE_TIME, ZoneOffset.UTC)
			);
		}
	}
}
