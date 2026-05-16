package com.bangpot.home.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.view.ExploreFiltersView;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;
import com.bangpot.explore.domain.view.ThemePreviewView;
import com.bangpot.home.application.service.AnonymousHomeReader;
import com.bangpot.home.application.service.GetHomeService;
import com.bangpot.home.application.usecase.GetHomeUseCase;
import com.bangpot.home.domain.view.HomeActivityRecordView;
import com.bangpot.home.domain.view.HomeMyCrewsView;
import com.bangpot.home.domain.view.HomeView;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.domain.view.MeetingActivityRecordView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;
import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;
import com.bangpot.user.domain.view.UserProfileView;
import com.bangpot.user.domain.view.UserSearchView;

class GetHomeServiceTest {

	private static final Instant BASE_TIME = Instant.parse("2026-04-19T00:00:00Z");

	private InMemoryUserQueryRepository userQueryRepository;
	private InMemoryCrewQueryRepository crewQueryRepository;
	private InMemoryMeetingQueryRepository meetingQueryRepository;
	private InMemoryExploreQueryRepository exploreQueryRepository;
	private GetHomeUseCase getHomeUseCase;

	@BeforeEach
	void setUp() {
		userQueryRepository = new InMemoryUserQueryRepository();
		crewQueryRepository = new InMemoryCrewQueryRepository();
		meetingQueryRepository = new InMemoryMeetingQueryRepository();
		exploreQueryRepository = new InMemoryExploreQueryRepository();
		getHomeUseCase = new GetHomeService(
			userQueryRepository,
			crewQueryRepository,
			meetingQueryRepository,
			exploreQueryRepository,
			new AnonymousHomeReader(crewQueryRepository, exploreQueryRepository),
			Clock.fixed(BASE_TIME, ZoneOffset.UTC)
		);
	}

	@Test
	void returnsGuestHomeWithEmptyPersonalizedSections() {
		crewQueryRepository.publicCrewPreviewView = PublicCrewPreviewView.of(List.of(
			PublicCrewPreviewView.Item.of(31L, "Alpha Crew", null, 12L)
		));
		exploreQueryRepository.themePreviewView = ThemePreviewView.of(List.of(
			ThemePreviewView.Item.of(
				101L,
				"Deep Blue",
				"Room Escape",
				"Seoul Mapo",
				"https://cdn.example.com/theme.jpg",
				7,
				false
			)
		));

		HomeView result = getHomeUseCase.handle(GetHomeUseCase.Query.of(null));

		assertThat(result.isLoggedIn()).isFalse();
		assertThat(result.myCrews().items()).isEmpty();
		assertThat(result.myCrews().totalCount()).isZero();
		assertThat(result.upcomingMeetings().nearestMeeting()).isNull();
		assertThat(result.upcomingMeetings().totalCount()).isZero();
		assertThat(result.activityRecord()).isEqualTo(HomeActivityRecordView.empty());
		assertThat(result.publicCrewPreview().items()).hasSize(1);
		assertThat(result.themeExplorePreview().items()).hasSize(1);
		assertThat(result.themeExplorePreview().items().getFirst().favoriteCount()).isEqualTo(7);
		assertThat(result.themeExplorePreview().items().getFirst().isFavorite()).isFalse();
	}

	@Test
	void returnsLoggedInHomeWithPersonalizedSummaries() {
		userQueryRepository.completedUserIds.add(7L);
		crewQueryRepository.myCrewsViewByUserId.put(
			7L,
			MyCrewsView.of(
				List.of(
					MyCrewsView.Item.of(11L, "Alpha Crew", CrewVisibility.PUBLIC, "leader-a", null),
					MyCrewsView.Item.of(12L, "Beta Crew", CrewVisibility.PRIVATE, "leader-b", null)
				),
				MyCrewsView.Page.of(0, 5, false)
			)
		);
		crewQueryRepository.myCrewsViewCountByUserId.put(7L, 3L);
		crewQueryRepository.publicCrewPreviewView = PublicCrewPreviewView.of(List.of(
			PublicCrewPreviewView.Item.of(31L, "Alpha Crew", null, 12L)
		));
		meetingQueryRepository.upcomingMeetingsViewByUserId.put(
			7L,
			UpcomingMeetingsView.of(
				List.of(
					UpcomingMeetingsView.Item.of(
						101L, "Theme A", "2026-04-20", "19:00"
					)
				),
				4L
			)
		);
		meetingQueryRepository.activityRecordViewByUserId.put(7L, MeetingActivityRecordView.of(4L, 3L));
		exploreQueryRepository.themePreviewView = ThemePreviewView.of(List.of(
			ThemePreviewView.Item.of(
				101L,
				"Deep Blue",
				"Room Escape",
				"Seoul Mapo",
				"https://cdn.example.com/theme.jpg",
				7,
				true
			)
		));

		HomeView result = getHomeUseCase.handle(GetHomeUseCase.Query.of(7L));

		assertThat(result.isLoggedIn()).isTrue();
		assertThat(result.myCrews().totalCount()).isEqualTo(3L);
		assertThat(result.myCrews().items()).extracting(HomeMyCrewsView.Item::crewId)
			.containsExactly(11L, 12L);
		assertThat(result.upcomingMeetings().totalCount()).isEqualTo(4L);
		assertThat(result.upcomingMeetings().nearestMeeting().meetingId()).isEqualTo(101L);
		assertThat(result.upcomingMeetings().nearestMeeting().themeName()).isEqualTo("Theme A");
		assertThat(result.activityRecord().completedCount()).isEqualTo(4L);
		assertThat(result.activityRecord().successRate()).isEqualTo(75);
		assertThat(result.publicCrewPreview().items()).hasSize(1);
		assertThat(result.themeExplorePreview().items()).hasSize(1);
		assertThat(result.themeExplorePreview().items().getFirst().favoriteCount()).isEqualTo(7);
		assertThat(result.themeExplorePreview().items().getFirst().isFavorite()).isTrue();
	}

	private static final class InMemoryUserQueryRepository implements UserQueryRepository {
		private final java.util.Set<Long> completedUserIds = new java.util.HashSet<>();

		@Override
		public UserProfileView findMyProfileUserViewByUserId(Long userId) {
			return null;
		}

		@Override
		public boolean existsCompletedUser(Long userId) {
			return completedUserIds.contains(userId);
		}

		@Override
		public UserSearchView searchUsersByNickname(String nickname, int page, int size) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		@Override
		public com.bangpot.crew.domain.view.ExploreCrewCardsView findExploreCrewCardsView(
			String keyword,
			com.bangpot.crew.domain.ExploreCrewSort sort,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewInviteCandidateAccessView>
			findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.CrewInviteCandidatesView findCrewInviteCandidatesView(
			Long crewId,
			Long leaderUserId,
			String nickname,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMemberAccessView>
			findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		private final Map<Long, MyCrewsView> myCrewsViewByUserId = new HashMap<>();
		private final Map<Long, Long> myCrewsViewCountByUserId = new HashMap<>();
		private final Map<Long, Long> activeCrewCountByUserId = new HashMap<>();
		private PublicCrewPreviewView publicCrewPreviewView = PublicCrewPreviewView.of(List.of());

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}
		@Override
		public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return myCrewsViewByUserId.getOrDefault(userId, MyCrewsView.of(List.of(), MyCrewsView.Page.of(page, size, false)));
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			return myCrewsViewCountByUserId.getOrDefault(userId, 0L);
		}

		@Override
		public PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
			return publicCrewPreviewView;
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			return activeCrewCountByUserId.getOrDefault(userId, 0L);
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			return 0L;
		}

		@Override
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			return com.bangpot.crew.domain.view.MeetingCreateCrewsView.of(List.of());
		}

		@Override
		public List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			return List.of();
		}
	}

	private static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {
		private final Map<Long, UpcomingMeetingsView> upcomingMeetingsViewByUserId = new HashMap<>();
		private final Map<Long, MeetingActivityRecordView> activityRecordViewByUserId = new HashMap<>();

		@Override
		public MyCalendarView findMyCalendarViewByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public UpcomingMeetingsView findUpcomingMeetingsViewByUserId(Long userId, int limit, String currentDate, String currentTime) {
			return upcomingMeetingsViewByUserId.getOrDefault(userId, UpcomingMeetingsView.of(List.of(), 0L));
		}

		@Override
		public MeetingActivityRecordView findActivityRecordViewByUserId(Long userId) {
			return activityRecordViewByUserId.getOrDefault(userId, MeetingActivityRecordView.empty());
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewScheduleView findCrewScheduleViewByCrewId(
			Long crewId,
			java.time.LocalDate from,
			java.time.LocalDate to
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewMeetingGalleryView findCrewMeetingGalleryView(
			Long crewId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.CrewMeetingGalleryView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.CrewMeetingGalleryView.Page.of(page, size, false)
			);
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(
			Long crewId,
			Long meetingId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public java.util.List<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(
			Long meetingId
		) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingDetailView> findMeetingDetailView(
			Long crewId,
			Long meetingId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return 0L;
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return 0L;
		}

		@Override
		public Map<Long, Integer> countCompletedByUserIds(java.util.Collection<Long> userIds) {
			return Map.of();
		}
	}

	private static final class InMemoryExploreQueryRepository implements ExploreQueryRepository {
		private ThemePreviewView themePreviewView = ThemePreviewView.of(List.of());

		@Override
		public ExploreThemeSearchView search(SearchCondition searchCondition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ExploreFiltersView getFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<ExploreThemeDetailView> getThemeDetail(Long themeId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames) {
			return Map.of();
		}

		@Override
		public ThemePreviewView findThemePreviewView(Long userId, int limit) {
			return themePreviewView;
		}
	}
}
