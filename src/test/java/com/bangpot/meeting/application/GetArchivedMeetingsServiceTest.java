package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.application.port.ArchivedMeetingReadRepository;
import com.bangpot.meeting.application.service.GetArchivedMeetingsService;
import com.bangpot.meeting.application.usecase.GetArchivedMeetingsUseCase;
import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetArchivedMeetingsServiceTest {

	private static final Instant NOW = Instant.parse("2026-04-14T09:00:00Z");

	private InMemoryUserRepository userRepository;
	private CompletedUserAccessService completedUserAccessService;
	private InMemoryArchivedMeetingReadRepository archiveMeetingReadRepository;
	private InMemoryExploreThemeReadRepository exploreThemeReadRepository;
	private GetArchivedMeetingsUseCase getArchivedMeetingsUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		archiveMeetingReadRepository = new InMemoryArchivedMeetingReadRepository();
		exploreThemeReadRepository = new InMemoryExploreThemeReadRepository();
		getArchivedMeetingsUseCase = new GetArchivedMeetingsService(
			completedUserAccessService,
			archiveMeetingReadRepository,
			exploreThemeReadRepository
		);
	}

	@Test
	void returnsCompletedArchiveCardsWithPosterFallback() {
		userRepository.save(fullUser(7L, "member"));
		archiveMeetingReadRepository.result = ArchivedMeetingReadRepository.SearchResult.of(
			List.of(
				ArchivedMeetingReadRepository.Item.of(31L, 101L, "Alpha Crew", "Deep Blue", "Hongdae", "2026-04-12", "SUCCESS"),
				ArchivedMeetingReadRepository.Item.of(30L, 102L, "Beta Crew", "Lost Harbor", "Busan", "2026-04-10", "FAILURE")
			),
			ArchivedMeetingReadRepository.PageInfo.of(0, 20, false)
		);
		exploreThemeReadRepository.posters.put("Deep Blue", "https://image.example/deep-blue.jpg");

		GetArchivedMeetingsUseCase.Result result = getArchivedMeetingsUseCase.handle(
			GetArchivedMeetingsUseCase.Query.of(7L, 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.items().get(0).meetingId()).isEqualTo(31L);
		assertThat(result.items().get(0).crewName()).isEqualTo("Alpha Crew");
		assertThat(result.items().get(0).posterImageUrl()).isEqualTo("https://image.example/deep-blue.jpg");
		assertThat(result.items().get(1).posterImageUrl()).isNull();
		assertThat(result.pageInfo().hasNext()).isFalse();
		assertThat(archiveMeetingReadRepository.lastUserId).isEqualTo(7L);
	}

	@Test
	void rejectsArchiveReadForIncompleteUser() {
		assertThatThrownBy(() -> getArchivedMeetingsUseCase.handle(GetArchivedMeetingsUseCase.Query.of(8L, 0, 20)))
			.isInstanceOf(AccessDeniedException.class);
	}

	private User fullUser(Long id, String nickname) {
		return User.rehydrate(id, nickname);
	}

	private static final class InMemoryArchivedMeetingReadRepository implements ArchivedMeetingReadRepository {
		private SearchResult result = SearchResult.of(List.of(), PageInfo.of(0, 20, false));
		private Long lastUserId;

		@Override
		public SearchResult search(Long userId, int page, int size) {
			lastUserId = userId;
			return result;
		}
	}

	private static final class InMemoryExploreThemeReadRepository implements ExploreThemeReadRepository {
		private final Map<String, String> posters = new HashMap<>();

		@Override
		public SearchResult search(Condition condition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.explore.application.usecase.GetExploreFiltersUseCase.Result getFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<ThemeDetail> getThemeDetail(Long themeId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames) {
			Map<String, String> result = new HashMap<>();
			for (String themeName : themeNames) {
				if (posters.containsKey(themeName)) {
					result.put(themeName, posters.get(themeName));
				}
			}
			return result;
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
			return users.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
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
}

