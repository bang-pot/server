package com.bangpot.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.bangpot.explore.application.exception.ExploreThemeNotFoundException;
import com.bangpot.explore.application.port.FavoriteThemeTargetRepository;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.service.AddThemeFavoriteService;
import com.bangpot.explore.application.service.RemoveThemeFavoriteService;
import com.bangpot.explore.application.usecase.AddThemeFavoriteUseCase;
import com.bangpot.explore.application.usecase.RemoveThemeFavoriteUseCase;
import com.bangpot.explore.domain.Theme;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class ThemeFavoriteMutationServiceTest {

	private InMemoryThemeRepository themeRepository;
	private InMemoryThemeFavoriteRepository themeFavoriteRepository;
	private InMemoryUserRepository userRepository;
	private AddThemeFavoriteUseCase addThemeFavoriteUseCase;
	private RemoveThemeFavoriteUseCase removeThemeFavoriteUseCase;

	@BeforeEach
	void setUp() {
		themeRepository = new InMemoryThemeRepository();
		themeFavoriteRepository = new InMemoryThemeFavoriteRepository();
		userRepository = new InMemoryUserRepository();
		CompletedUserAccessService completedUserAccessService = new CompletedUserAccessService(userRepository);
		addThemeFavoriteUseCase = new AddThemeFavoriteService(
			completedUserAccessService,
			themeRepository,
			themeFavoriteRepository
		);
		removeThemeFavoriteUseCase = new RemoveThemeFavoriteService(
			completedUserAccessService,
			themeRepository,
			themeFavoriteRepository
		);
	}

	@Test
	void addsFavoriteAndIncreasesFavoriteCount() {
		userRepository.save(User.create(7L, "alpha"));
		Theme theme = themeRepository.save(activeTheme(5L));

		AddThemeFavoriteUseCase.Result result = addThemeFavoriteUseCase.handle(AddThemeFavoriteUseCase.Command.of(7L, 5L));

		assertThat(result.themeId()).isEqualTo(5L);
		assertThat(result.isFavorite()).isTrue();
		assertThat(result.favoriteCount()).isEqualTo(1);
		assertThat(themeFavoriteRepository.exists(7L, 5L)).isTrue();
		assertThat(theme.getFavoriteCount()).isEqualTo(1);
	}

	@Test
	void ignoresDuplicateFavoriteRequest() {
		userRepository.save(User.create(7L, "alpha"));
		Theme theme = themeRepository.save(activeTheme(5L));
		themeFavoriteRepository.create(7L, 5L, Instant.now());
		theme.increaseFavoriteCount();

		AddThemeFavoriteUseCase.Result result = addThemeFavoriteUseCase.handle(AddThemeFavoriteUseCase.Command.of(7L, 5L));

		assertThat(result.favoriteCount()).isEqualTo(1);
		assertThat(theme.getFavoriteCount()).isEqualTo(1);
	}

	@Test
	void removesFavoriteAndDecreasesFavoriteCount() {
		userRepository.save(User.create(7L, "alpha"));
		Theme theme = themeRepository.save(activeTheme(5L));
		themeFavoriteRepository.create(7L, 5L, Instant.now());
		theme.increaseFavoriteCount();

		RemoveThemeFavoriteUseCase.Result result = removeThemeFavoriteUseCase.handle(
			RemoveThemeFavoriteUseCase.Command.of(7L, 5L)
		);

		assertThat(result.themeId()).isEqualTo(5L);
		assertThat(result.isFavorite()).isFalse();
		assertThat(result.favoriteCount()).isEqualTo(0);
		assertThat(themeFavoriteRepository.exists(7L, 5L)).isFalse();
		assertThat(theme.getFavoriteCount()).isZero();
	}

	@Test
	void ignoresDeleteWhenFavoriteDoesNotExist() {
		userRepository.save(User.create(7L, "alpha"));
		Theme theme = themeRepository.save(activeTheme(5L));

		RemoveThemeFavoriteUseCase.Result result = removeThemeFavoriteUseCase.handle(
			RemoveThemeFavoriteUseCase.Command.of(7L, 5L)
		);

		assertThat(result.favoriteCount()).isZero();
		assertThat(theme.getFavoriteCount()).isZero();
	}

	@Test
	void rejectsIncompleteUser() {
		themeRepository.save(activeTheme(5L));

		assertThatThrownBy(() -> addThemeFavoriteUseCase.handle(AddThemeFavoriteUseCase.Command.of(7L, 5L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void throwsWhenThemeDoesNotExist() {
		userRepository.save(User.create(7L, "alpha"));

		assertThatThrownBy(() -> removeThemeFavoriteUseCase.handle(RemoveThemeFavoriteUseCase.Command.of(7L, 999L)))
			.isInstanceOf(ExploreThemeNotFoundException.class);
	}

	private Theme activeTheme(Long id) {
		Theme theme = Theme.create(1L, "Deep Blue", "HORROR", null, 4, "HIGH", "2-4 players", 60);
		ReflectionTestUtils.setField(theme, "id", id);
		return theme;
	}

	private static final class InMemoryThemeRepository implements FavoriteThemeTargetRepository {

		private final Map<Long, Theme> themes = new HashMap<>();

		@Override
		public Optional<Theme> findActiveById(Long themeId) {
			Theme theme = themes.get(themeId);
			if (theme == null || !theme.isActive()) {
				return Optional.empty();
			}
			return Optional.of(theme);
		}

		@Override
		public Theme save(Theme theme) {
			themes.put(theme.getId(), theme);
			return theme;
		}
	}

	private static final class InMemoryThemeFavoriteRepository implements ThemeFavoriteRepository {

		private final Map<Long, Set<Long>> favoriteThemeIdsByUserId = new HashMap<>();

		@Override
		public boolean create(Long userId, Long themeId, Instant createdAt) {
			return favoriteThemeIdsByUserId.computeIfAbsent(userId, ignored -> new HashSet<>()).add(themeId);
		}

		@Override
		public boolean delete(Long userId, Long themeId) {
			Set<Long> favoriteThemeIds = favoriteThemeIdsByUserId.get(userId);
			if (favoriteThemeIds == null) {
				return false;
			}
			return favoriteThemeIds.remove(themeId);
		}

		@Override
		public boolean exists(Long userId, Long themeId) {
			return favoriteThemeIdsByUserId.getOrDefault(userId, Set.of()).contains(themeId);
		}

		@Override
		public Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds) {
			Set<Long> favoriteThemeIds = favoriteThemeIdsByUserId.getOrDefault(userId, Set.of());
			Set<Long> result = new HashSet<>();
			for (Long themeId : themeIds) {
				if (favoriteThemeIds.contains(themeId)) {
					result.add(themeId);
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
			throw new UnsupportedOperationException();
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			throw new UnsupportedOperationException();
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}
	}
}
