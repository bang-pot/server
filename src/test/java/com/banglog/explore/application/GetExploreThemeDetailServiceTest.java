package com.banglog.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.banglog.explore.application.exception.ExploreThemeNotFoundException;
import com.banglog.explore.application.port.ExploreQueryRepository;
import com.banglog.explore.application.port.ThemeFavoriteRepository;
import com.banglog.explore.application.service.GetExploreThemeDetailService;
import com.banglog.explore.application.usecase.GetExploreThemeDetailUseCase;
import com.banglog.explore.domain.view.ExploreFiltersView;
import com.banglog.explore.domain.view.ExploreThemeDetailView;
import com.banglog.explore.domain.view.ExploreThemeSearchView;
import com.banglog.explore.domain.view.ThemePreviewView;

class GetExploreThemeDetailServiceTest {

	private InMemoryExploreQueryRepository repository;
	private InMemoryThemeFavoriteRepository themeFavoriteRepository;
	private GetExploreThemeDetailUseCase useCase;

	@BeforeEach
	void setUp() {
		repository = new InMemoryExploreQueryRepository();
		themeFavoriteRepository = new InMemoryThemeFavoriteRepository();
		useCase = new GetExploreThemeDetailService(repository, themeFavoriteRepository);
	}

	@Test
	void returnsThemeDetailWithRelatedThemes() {
		repository.detail = ExploreThemeDetailView.of(
			5L,
			"Deep Blue",
			1L,
			"Seoul Escape Hongdae",
			"Seoul Mapo",
			List.of("HORROR"),
			"https://image.example/deep-blue.jpg",
			4,
			60,
			"Deep sea mystery theme",
			"https://example.com/deep-blue",
			List.of(
				ExploreThemeDetailView.RelatedTheme.of(
					1L,
					"Laugh Track",
					1L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					List.of("COMEDY"),
					null,
					2,
					50,
					4
				),
				ExploreThemeDetailView.RelatedTheme.of(
					3L,
					"Time Attack",
					1L,
					"Seoul Escape Hongdae",
					"Seoul Mapo",
					List.of("THRILLER"),
					null,
					3,
					75,
					1
				)
			)
		);

		themeFavoriteRepository.favorite(7L, 5L);
		themeFavoriteRepository.favorite(7L, 1L);

		ExploreThemeDetailView result = useCase.handle(GetExploreThemeDetailUseCase.Query.of(7L, 5L));

		assertThat(result.themeId()).isEqualTo(5L);
		assertThat(result.themeName()).isEqualTo("Deep Blue");
		assertThat(result.isFavorite()).isTrue();
		assertThat(result.relatedThemes()).hasSize(2);
		assertThat(result.relatedThemes())
			.extracting(ExploreThemeDetailView.RelatedTheme::themeName)
			.containsExactly("Laugh Track", "Time Attack");
		assertThat(result.relatedThemes())
			.extracting(ExploreThemeDetailView.RelatedTheme::favoriteCount)
			.containsExactly(4, 1);
		assertThat(result.relatedThemes())
			.extracting(ExploreThemeDetailView.RelatedTheme::isFavorite)
			.containsExactly(true, false);
	}

	@Test
	void throwsWhenThemeDoesNotExist() {
		assertThatThrownBy(() -> useCase.handle(GetExploreThemeDetailUseCase.Query.of(null, 999L)))
			.isInstanceOf(ExploreThemeNotFoundException.class);
	}

	private static final class InMemoryExploreQueryRepository implements ExploreQueryRepository {

		private ExploreThemeDetailView detail;

		@Override
		public ExploreThemeSearchView search(SearchCondition searchCondition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ExploreFiltersView getFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<ExploreThemeDetailView> getThemeDetail(Long themeId) {
			if (detail == null || !detail.themeId().equals(themeId)) {
				return Optional.empty();
			}
			return Optional.of(detail);
		}

		@Override
		public Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames) {
			return new HashMap<>();
		}

		@Override
		public ThemePreviewView findThemePreviewView(Long userId, int limit) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryThemeFavoriteRepository implements ThemeFavoriteRepository {
		private final Map<Long, Set<Long>> favoriteThemeIdsByUserId = new HashMap<>();

		@Override
		public boolean create(Long userId, Long themeId, java.time.Instant createdAt) {
			return favoriteThemeIdsByUserId.computeIfAbsent(userId, ignored -> new java.util.HashSet<>()).add(themeId);
		}

		@Override
		public boolean delete(Long userId, Long themeId) {
			return favoriteThemeIdsByUserId.getOrDefault(userId, Set.of()).remove(themeId);
		}

		@Override
		public Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds) {
			Set<Long> favorites = favoriteThemeIdsByUserId.getOrDefault(userId, Set.of());
			return themeIds.stream()
				.filter(favorites::contains)
				.collect(java.util.stream.Collectors.toSet());
		}

		void favorite(Long userId, Long themeId) {
			favoriteThemeIdsByUserId.computeIfAbsent(userId, ignored -> new java.util.HashSet<>()).add(themeId);
		}
	}
}
