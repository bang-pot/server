package com.bangpot.explore.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.application.port.ThemeFavoriteRepository;
import com.bangpot.explore.application.service.GetExploreFiltersService;
import com.bangpot.explore.application.service.GetExploreThemesService;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;
import com.bangpot.explore.domain.view.ExploreFiltersView;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;
import com.bangpot.explore.domain.view.ThemePreviewView;

class ExploreThemeSearchServiceTest {

	private InMemoryExploreQueryRepository repository;
	private InMemoryThemeFavoriteRepository themeFavoriteRepository;
	private GetExploreThemesUseCase getExploreThemesUseCase;
	private GetExploreFiltersUseCase getExploreFiltersUseCase;

	@BeforeEach
	void setUp() {
		repository = new InMemoryExploreQueryRepository();
		themeFavoriteRepository = new InMemoryThemeFavoriteRepository();
		getExploreThemesUseCase = new GetExploreThemesService(repository, themeFavoriteRepository);
		getExploreFiltersUseCase = new GetExploreFiltersService(repository);
	}

	@Test
	void searchesThemesByKeywordAcrossThemeStoreAndRegion() {
		repository.append(
			1L, 101L, "Deep Blue", "Seoul Escape Hongdae", "Seoul", "Mapo",
			List.of("HORROR", "THRILLER"), "https://image.example/deep-blue.jpg", 4, "HIGH", "2-4 players", 60, 0
		);
		repository.append(
			2L, 102L, "Time Attack", "Busan Escape Haeundae", "Busan", "Haeundae",
			List.of("THRILLER"), null, 3, "MEDIUM", "3-5 players", 75, 0
		);

		ExploreThemeSearchView result = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of("Hongdae", List.of(), null, null, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().getFirst().themeName()).isEqualTo("Deep Blue");

		ExploreThemeSearchView byThemeName = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of("Time", List.of(), null, null, 0, 20)
		);

		assertThat(byThemeName.items()).hasSize(1);
		assertThat(byThemeName.items().getFirst().storeName()).isEqualTo("Busan Escape Haeundae");

		ExploreThemeSearchView byRegion = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of("Busan", List.of(), null, null, 0, 20)
		);

		assertThat(byRegion.items()).hasSize(1);
		assertThat(byRegion.items().getFirst().themeId()).isEqualTo(2L);
	}

	@Test
	void appliesGenreAndRegionFiltersWithPagination() {
		repository.append(1L, 101L, "Deep Blue", "Store A", "Seoul", "Gangnam", List.of("HORROR", "THRILLER"), null, 4, "HIGH", "2-4 players", 60, 0);
		repository.append(2L, 101L, "Lost Temple", "Store A", "Seoul", "Gangnam", List.of("HORROR"), null, 3, "MEDIUM", "3-5 players", 75, 0);
		repository.append(3L, 102L, "Comedy Room", "Store B", "Seoul", "Mapo", List.of("COMEDY"), null, 2, "LOW", "2-3 players", 50, 0);

		ExploreThemeSearchView firstPage = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of(null, List.of("HORROR"), "Seoul", "Gangnam", 0, 1)
		);

		assertThat(firstPage.items()).hasSize(1);
		assertThat(firstPage.pageInfo().page()).isEqualTo(0);
		assertThat(firstPage.pageInfo().size()).isEqualTo(1);
		assertThat(firstPage.pageInfo().hasNext()).isTrue();

		ExploreThemeSearchView secondPage = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of(null, List.of("HORROR"), "Seoul", "Gangnam", 1, 1)
		);

		assertThat(secondPage.items()).hasSize(1);
		assertThat(secondPage.pageInfo().hasNext()).isFalse();
		assertThat(secondPage.items())
			.extracting(ExploreThemeSearchView.Item::genres)
			.allMatch(genres -> genres.contains("HORROR"));
	}

	@Test
	void marksFavoritedThemesForAuthenticatedUserOnly() {
		repository.append(
			1L, 101L, "Deep Blue", "Store A", "Seoul", "Gangnam",
			List.of("HORROR"), null, 4, "HIGH", "2-4 players", 60, 3
		);
		repository.append(
			2L, 101L, "Laugh Track", "Store A", "Seoul", "Gangnam",
			List.of("COMEDY"), null, 2, "LOW", "2-4 players", 50, 1
		);
		themeFavoriteRepository.favorite(7L, 1L);

		ExploreThemeSearchView guestResult = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of(null, null, List.of(), null, null, 0, 20)
		);
		ExploreThemeSearchView userResult = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of(7L, null, List.of(), null, null, 0, 20)
		);

		assertThat(guestResult.items()).extracting(ExploreThemeSearchView.Item::isFavorite)
			.containsExactly(false, false);
		assertThat(userResult.items()).extracting(ExploreThemeSearchView.Item::isFavorite)
			.containsExactly(false, true);
	}

	@Test
	void skipsFavoriteLookupWhenAuthenticatedSearchResultIsEmpty() {
		repository.append(
			1L, 101L, "Deep Blue", "Store A", "Seoul", "Gangnam",
			List.of("HORROR"), null, 4, "HIGH", "2-4 players", 60, 3
		);

		ExploreThemeSearchView result = getExploreThemesUseCase.handle(
			GetExploreThemesUseCase.Query.of(7L, "No Match", List.of(), null, null, 0, 20)
		);

		assertThat(result.items()).isEmpty();
		assertThat(themeFavoriteRepository.findFavoritedThemeIdsCallCount).isZero();
	}

	@Test
	void returnsFilterOptionsGroupedByRegion() {
		repository.append(1L, 101L, "Deep Blue", "Store A", "Seoul", "Gangnam", List.of("HORROR", "THRILLER"), null, 4, "HIGH", "2-4 players", 60, 0);
		repository.append(2L, 102L, "Lost Temple", "Store B", "Seoul", "Mapo", List.of("THRILLER"), null, 3, "MEDIUM", "3-5 players", 75, 0);
		repository.append(3L, 103L, "Comedy Room", "Store C", "Busan", "Haeundae", List.of("COMEDY"), null, 2, "LOW", "2-3 players", 50, 0);

		ExploreFiltersView result = getExploreFiltersUseCase.handle();

		assertThat(result.genres()).containsExactly("COMEDY", "HORROR", "THRILLER");
		assertThat(result.regions()).hasSize(2);
		assertThat(result.regions().getFirst().name()).isEqualTo("Busan");
		assertThat(result.regions().getFirst().districts()).containsExactly("Haeundae");
		assertThat(result.regions().get(1).name()).isEqualTo("Seoul");
		assertThat(result.regions().get(1).districts()).containsExactly("Gangnam", "Mapo");
	}

	private static final class InMemoryExploreQueryRepository implements ExploreQueryRepository {

		private final List<Row> rows = new ArrayList<>();

		void append(
			Long themeId,
			Long storeId,
			String themeName,
			String storeName,
			String region,
			String district,
			List<String> genres,
			String posterImageUrl,
			Integer difficulty,
			String activityLabel,
			String recommendedPlayers,
			Integer runningTimeMinutes,
			Integer favoriteCount
		) {
			rows.add(new Row(
				themeId,
				storeId,
				themeName,
				storeName,
				region,
				district,
				genres,
				posterImageUrl,
				difficulty,
				activityLabel,
				recommendedPlayers,
				runningTimeMinutes,
				favoriteCount
			));
		}

		@Override
		public ExploreThemeSearchView search(SearchCondition searchCondition) {
			List<Row> filtered = rows.stream()
				.filter(row -> matchesKeyword(row, searchCondition.keyword()))
				.filter(row -> matchesGenres(row, searchCondition.genres()))
				.filter(row -> matchesRegion(row, searchCondition.region()))
				.filter(row -> matchesDistrict(row, searchCondition.district()))
				.sorted(Comparator.comparing(Row::themeId).reversed())
				.toList();

			int fromIndex = Math.min(searchCondition.page() * searchCondition.size(), filtered.size());
			int toIndex = Math.min(fromIndex + searchCondition.size(), filtered.size());
			List<ExploreThemeSearchView.Item> items = filtered.subList(fromIndex, toIndex).stream()
				.map(row -> ExploreThemeSearchView.Item.of(
					row.themeId(),
					row.themeName(),
					row.storeId(),
					row.storeName(),
					row.region() + " " + row.district(),
					row.genres(),
					row.posterImageUrl(),
					row.difficulty(),
					row.activityLabel(),
					row.recommendedPlayers(),
					row.runningTimeMinutes(),
					row.favoriteCount()
				))
				.toList();

			boolean hasNext = toIndex < filtered.size();
			int totalPages = filtered.isEmpty()
				? 0
				: (int)Math.ceil((double)filtered.size() / searchCondition.size());
			return ExploreThemeSearchView.of(
				items,
				ExploreThemeSearchView.PageInfo.of(
					searchCondition.page(),
					searchCondition.size(),
					hasNext,
					filtered.size(),
					totalPages
				)
			);
		}

		@Override
		public ExploreFiltersView getFilters() {
			List<String> genres = rows.stream()
				.flatMap(row -> row.genres().stream())
				.distinct()
				.sorted()
				.toList();

			List<ExploreFiltersView.Region> regions = rows.stream()
				.collect(java.util.stream.Collectors.groupingBy(Row::region))
				.entrySet()
				.stream()
				.map(entry -> ExploreFiltersView.Region.of(
					entry.getKey(),
					entry.getValue().stream()
						.map(Row::district)
						.distinct()
						.sorted()
						.toList()
				))
				.sorted(Comparator.comparing(ExploreFiltersView.Region::name))
				.toList();

			return ExploreFiltersView.of(genres, regions);
		}

		@Override
		public Optional<ExploreThemeDetailView> getThemeDetail(Long themeId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames) {
			return new HashMap<>();
		}

		@Override
		public ThemePreviewView findThemePreviewView(Long userId, int limit) {
			throw new UnsupportedOperationException();
		}

		private boolean matchesKeyword(Row row, String keyword) {
			if (keyword == null || keyword.isBlank()) {
				return true;
			}
			return contains(row.themeName(), keyword)
				|| contains(row.storeName(), keyword)
				|| contains(row.region(), keyword)
				|| contains(row.district(), keyword);
		}

		private boolean matchesGenres(Row row, List<String> genres) {
			return genres == null || genres.isEmpty() || row.genres().stream().anyMatch(genres::contains);
		}

		private boolean matchesRegion(Row row, String region) {
			return region == null || region.isBlank() || region.equals(row.region());
		}

		private boolean matchesDistrict(Row row, String district) {
			return district == null || district.isBlank() || district.equals(row.district());
		}

		private boolean contains(String value, String keyword) {
			return value != null && value.toLowerCase().contains(keyword.toLowerCase());
		}

		private record Row(
			Long themeId,
			Long storeId,
			String themeName,
			String storeName,
			String region,
			String district,
			List<String> genres,
			String posterImageUrl,
			Integer difficulty,
			String activityLabel,
			String recommendedPlayers,
			Integer runningTimeMinutes,
			Integer favoriteCount
		) {
		}
	}

	private static final class InMemoryThemeFavoriteRepository implements ThemeFavoriteRepository {
		private final Map<Long, Set<Long>> favoriteThemeIdsByUserId = new HashMap<>();
		private int findFavoritedThemeIdsCallCount;

		@Override
		public boolean create(Long userId, Long themeId, java.time.Instant createdAt) {
			return favoriteThemeIdsByUserId.computeIfAbsent(userId, ignored -> new HashSet<>()).add(themeId);
		}

		@Override
		public boolean delete(Long userId, Long themeId) {
			return favoriteThemeIdsByUserId.getOrDefault(userId, Set.of()).remove(themeId);
		}

		@Override
		public Set<Long> findFavoritedThemeIds(Long userId, List<Long> themeIds) {
			findFavoritedThemeIdsCallCount++;
			Set<Long> favorites = favoriteThemeIdsByUserId.getOrDefault(userId, Set.of());
			return themeIds.stream()
				.filter(favorites::contains)
				.collect(java.util.stream.Collectors.toSet());
		}

		void favorite(Long userId, Long themeId) {
			favoriteThemeIdsByUserId.computeIfAbsent(userId, ignored -> new HashSet<>()).add(themeId);
		}
	}
}
