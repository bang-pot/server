package com.bangpot.home.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;
import com.bangpot.home.application.port.HomePublicCrewPreviewReadRepository;
import com.bangpot.home.application.port.HomeUpcomingMeetingReadRepository;
import com.bangpot.home.application.service.GetHomeService;
import com.bangpot.home.application.usecase.GetHomeUseCase;
import com.bangpot.user.application.port.MyCrewReadRepository;
import com.bangpot.user.application.port.ProfileHubReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

class GetHomeServiceTest {

	private static final Instant BASE_TIME = Instant.parse("2026-04-19T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryProfileHubReadRepository profileHubReadRepository;
	private InMemoryMyCrewReadRepository myCrewReadRepository;
	private InMemoryHomeUpcomingMeetingReadRepository homeUpcomingMeetingReadRepository;
	private InMemoryHomePublicCrewPreviewReadRepository homePublicCrewPreviewReadRepository;
	private StubExploreThemesUseCase exploreThemesUseCase;
	private GetHomeUseCase getHomeUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		profileHubReadRepository = new InMemoryProfileHubReadRepository();
		myCrewReadRepository = new InMemoryMyCrewReadRepository();
		homeUpcomingMeetingReadRepository = new InMemoryHomeUpcomingMeetingReadRepository();
		homePublicCrewPreviewReadRepository = new InMemoryHomePublicCrewPreviewReadRepository();
		exploreThemesUseCase = new StubExploreThemesUseCase();
		getHomeUseCase = new GetHomeService(
			authUserRepository,
			userRepository,
			profileHubReadRepository,
			myCrewReadRepository,
			homeUpcomingMeetingReadRepository,
			homePublicCrewPreviewReadRepository,
			exploreThemesUseCase,
			Clock.fixed(BASE_TIME, ZoneOffset.UTC)
		);
	}

	@Test
	void returnsGuestHomeWithEmptyPersonalizedSections() {
		homePublicCrewPreviewReadRepository.setItems(List.of(
			HomePublicCrewPreviewReadRepository.Item.of(31L, "Alpha Crew", null, 12L, true)
		));
		exploreThemesUseCase.result = GetExploreThemesUseCase.Result.of(
			List.of(
				GetExploreThemesUseCase.Item.of(
					101L, "Deep Blue", 501L, "Room Escape", "Seoul Mapo", "Mystery",
					"https://cdn.example.com/theme.jpg", null, null, null, null, 0, false
				)
			),
			GetExploreThemesUseCase.PageInfo.of(0, 8, false)
		);

		GetHomeUseCase.Result result = getHomeUseCase.handle(GetHomeUseCase.Query.of(null));

		assertThat(result.isLoggedIn()).isFalse();
		assertThat(result.cta().canExplorePublicCrews()).isTrue();
		assertThat(result.cta().canCreateCrew()).isFalse();
		assertThat(result.myCrews().items()).isEmpty();
		assertThat(result.myCrews().totalCount()).isZero();
		assertThat(result.upcomingMeetings().items()).isEmpty();
		assertThat(result.upcomingMeetings().totalCount()).isZero();
		assertThat(result.publicCrewPreview().items()).hasSize(1);
		assertThat(result.themeExplorePreview().items()).hasSize(1);
	}

	@Test
	void returnsLoggedInHomeWithPersonalizedSummaries() {
		authUserRepository.save(fullUser(7L, "bangpot"));
		userRepository.save(User.rehydrate(7L, "bangpot", true));
		profileHubReadRepository.putCounts(7L, 0L, 0L, 3L, 0L);
		myCrewReadRepository.resultByUserId.put(
			7L,
			MyCrewReadRepository.SearchResult.of(
				List.of(
					MyCrewReadRepository.Item.of(11L, "Alpha Crew", "PUBLIC", "leader-a", null),
					MyCrewReadRepository.Item.of(12L, "Beta Crew", "PRIVATE", "leader-b", null)
				),
				MyCrewReadRepository.PageInfo.of(0, 5, false)
			)
		);
		homeUpcomingMeetingReadRepository.resultByUserId.put(
			7L,
			HomeUpcomingMeetingReadRepository.Result.of(
				List.of(
					HomeUpcomingMeetingReadRepository.Item.of(
						101L, "Friday Escape", 11L, "Alpha Crew", "2026-04-20", "19:00", "RECRUITING"
					)
				),
				4L
			)
		);
		homePublicCrewPreviewReadRepository.setItems(List.of(
			HomePublicCrewPreviewReadRepository.Item.of(31L, "Alpha Crew", null, 12L, true)
		));
		exploreThemesUseCase.result = GetExploreThemesUseCase.Result.of(
			List.of(
				GetExploreThemesUseCase.Item.of(
					101L, "Deep Blue", 501L, "Room Escape", "Seoul Mapo", "Mystery",
					"https://cdn.example.com/theme.jpg", null, null, null, null, 0, false
				)
			),
			GetExploreThemesUseCase.PageInfo.of(0, 8, false)
		);

		GetHomeUseCase.Result result = getHomeUseCase.handle(GetHomeUseCase.Query.of(7L));

		assertThat(result.isLoggedIn()).isTrue();
		assertThat(result.cta().canCreateCrew()).isTrue();
		assertThat(result.myCrews().totalCount()).isEqualTo(3L);
		assertThat(result.myCrews().items()).extracting(GetHomeUseCase.MyCrewItem::crewId)
			.containsExactly(11L, 12L);
		assertThat(result.upcomingMeetings().totalCount()).isEqualTo(4L);
		assertThat(result.upcomingMeetings().items()).extracting(GetHomeUseCase.UpcomingMeetingItem::meetingId)
			.containsExactly(101L);
		assertThat(result.publicCrewPreview().items()).hasSize(1);
		assertThat(result.themeExplorePreview().items()).hasSize(1);
	}

	private AuthUser fullUser(Long id, String nickname) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			"provider-" + id,
			AuthUserStatus.FULL,
			nickname,
			RequiredTermsAgreement.of("2026-04-14", BASE_TIME),
			null,
			BASE_TIME,
			BASE_TIME
		);
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {
		private final Map<Long, AuthUser> users = new HashMap<>();

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(users.get(userId)).filter(user -> !user.isWithdrawn());
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			return Optional.empty();
		}

		@Override
		public AuthUser save(AuthUser user) {
			users.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {
		private final Map<Long, User> users = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(users.get(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return false;
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return List.of();
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryProfileHubReadRepository implements ProfileHubReadRepository {
		private final Map<Long, Counts> countsByUserId = new HashMap<>();

		@Override
		public Counts loadCounts(Long userId) {
			return countsByUserId.getOrDefault(userId, Counts.of(0L, 0L, 0L, 0L));
		}

		void putCounts(Long userId, long createdMeetingsCount, long joinedMeetingsCount, long myCrewsCount, long pendingCrewsCount) {
			countsByUserId.put(userId, Counts.of(createdMeetingsCount, joinedMeetingsCount, myCrewsCount, pendingCrewsCount));
		}
	}

	private static final class InMemoryMyCrewReadRepository implements MyCrewReadRepository {
		private final Map<Long, SearchResult> resultByUserId = new HashMap<>();

		@Override
		public SearchResult search(Long userId, int page, int size) {
			return resultByUserId.getOrDefault(userId, SearchResult.of(List.of(), PageInfo.of(page, size, false)));
		}
	}

	private static final class InMemoryHomeUpcomingMeetingReadRepository implements HomeUpcomingMeetingReadRepository {
		private final Map<Long, Result> resultByUserId = new HashMap<>();

		@Override
		public Result findUpcomingMeetings(Long userId, int limit, String currentDate, String currentTime) {
			return resultByUserId.getOrDefault(userId, Result.of(List.of(), 0L));
		}
	}

	private static final class InMemoryHomePublicCrewPreviewReadRepository implements HomePublicCrewPreviewReadRepository {
		private List<Item> items = List.of();

		@Override
		public List<Item> findPreviewItems(int limit) {
			return items;
		}

		void setItems(List<Item> items) {
			this.items = items;
		}
	}

	private static final class StubExploreThemesUseCase implements GetExploreThemesUseCase {
		private Result result = Result.of(List.of(), PageInfo.of(0, 8, false));

		@Override
		public Result handle(Query query) {
			return result;
		}
	}
}
