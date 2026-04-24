package com.bangpot.explore.infrastructure;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.domain.view.ExploreFiltersView;
import com.bangpot.explore.domain.view.ExploreThemeDetailView;
import com.bangpot.explore.domain.view.ExploreThemeSearchView;
import com.bangpot.explore.domain.view.ThemePreviewView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaExploreQueryRepository implements ExploreQueryRepository {

	private static final String UNUSED_GENRE = "__UNUSED__";

	private final ThemeJpaRepository themeJpaRepository;
	private final StoreJpaRepository storeJpaRepository;
	private final ThemeFavoriteJpaRepository themeFavoriteJpaRepository;

	@Override
	public ExploreThemeSearchView search(SearchCondition searchCondition) {
		List<String> genres = normalizedGenres(searchCondition.genres());
		boolean keywordEmpty = isBlank(searchCondition.keyword());
		boolean regionEmpty = isBlank(searchCondition.region());
		boolean districtEmpty = isBlank(searchCondition.district());
		Slice<ThemeJpaRepository.ThemeCardProjection> slice = themeJpaRepository.search(
			keywordEmpty,
			keywordPattern(searchCondition.keyword()),
			genres,
			searchCondition.genres() == null || searchCondition.genres().isEmpty(),
			regionEmpty,
			normalizedText(searchCondition.region()),
			districtEmpty,
			normalizedText(searchCondition.district()),
			PageRequest.of(searchCondition.page(), searchCondition.size())
		);

		List<ExploreThemeSearchView.Item> items = slice.getContent().stream()
			.map(this::toItem)
			.toList();

		return ExploreThemeSearchView.of(
			items,
			ExploreThemeSearchView.PageInfo.of(searchCondition.page(), searchCondition.size(), slice.hasNext())
		);
	}

	@Override
	public ExploreFiltersView getFilters() {
		List<String> genres = themeJpaRepository.findActiveGenres();
		List<StoreJpaRepository.RegionDistrictProjection> regionRows = storeJpaRepository.findActiveRegionDistricts();

		Map<String, Set<String>> grouped = new LinkedHashMap<>();
		for (StoreJpaRepository.RegionDistrictProjection row : regionRows) {
			String region = row.getRegion();
			String district = row.getDistrict();
			grouped.computeIfAbsent(region, key -> new LinkedHashSet<>());
			if (district != null && !district.isBlank()) {
				grouped.get(region).add(district);
			}
		}

		List<ExploreFiltersView.Region> regions = grouped.entrySet().stream()
			.map(entry -> ExploreFiltersView.Region.of(entry.getKey(), List.copyOf(entry.getValue())))
			.toList();

		return ExploreFiltersView.of(genres, regions);
	}

	@Override
	public Optional<ExploreThemeDetailView> getThemeDetail(Long themeId) {
		return themeJpaRepository.findActiveThemeDetailById(themeId)
			.map(row -> ExploreThemeDetailView.of(
				row.getThemeId(),
				row.getThemeName(),
				row.getStoreId(),
				row.getStoreName(),
				toRegionLabel(row.getRegion(), row.getDistrict()),
				row.getGenre(),
				row.getPosterImageUrl(),
				row.getDifficulty(),
				row.getRunningTimeMinutes(),
				row.getDescription(),
				row.getExternalLink(),
				themeJpaRepository.findRelatedActiveThemes(row.getStoreId(), row.getThemeId(), PageRequest.of(0, 4))
					.stream()
					.map(this::toRelatedThemeSummary)
					.toList()
			));
	}

	@Override
	public Map<String, String> getPosterImageUrlsByThemeNames(List<String> themeNames) {
		if (themeNames == null || themeNames.isEmpty()) {
			return Map.of();
		}

		Map<String, String> posters = new LinkedHashMap<>();
		for (ThemeJpaRepository.ThemePosterProjection projection : themeJpaRepository.findActivePosterImagesByThemeNames(themeNames)) {
			posters.putIfAbsent(projection.getThemeName(), projection.getPosterImageUrl());
		}
		return posters;
	}

	@Override
	public ThemePreviewView findThemePreviewView(Long userId, int limit) {
		List<ExploreThemeSearchView.Item> items = search(SearchCondition.of(null, null, null, null, 0, limit)).items();
		if (userId == null || items.isEmpty()) {
			return ThemePreviewView.of(items.stream()
				.map(item -> ThemePreviewView.Item.of(
					item.themeId(),
					item.themeName(),
					item.storeName(),
					item.regionLabel(),
					item.posterImageUrl(),
					item.favoriteCount(),
					false
				))
				.toList());
		}

		List<Long> themeIds = items.stream()
			.map(ExploreThemeSearchView.Item::themeId)
			.toList();
		Set<Long> favoritedThemeIds = Set.copyOf(themeFavoriteJpaRepository.findThemeIdsByUserIdAndThemeIdIn(userId, themeIds));
		return ThemePreviewView.of(items.stream()
			.map(item -> ThemePreviewView.Item.of(
				item.themeId(),
				item.themeName(),
				item.storeName(),
				item.regionLabel(),
				item.posterImageUrl(),
				item.favoriteCount(),
				favoritedThemeIds.contains(item.themeId())
			))
			.toList());
	}

	private List<String> normalizedGenres(List<String> genres) {
		if (genres == null || genres.isEmpty()) {
			return List.of(UNUSED_GENRE);
		}
		return genres;
	}

	private String normalizedText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String keywordPattern(String keyword) {
		if (isBlank(keyword)) {
			return "%";
		}
		return "%" + keyword.toLowerCase() + "%";
	}

	private ExploreThemeSearchView.Item toItem(ThemeJpaRepository.ThemeCardProjection row) {
		return ExploreThemeSearchView.Item.of(
			row.getThemeId(),
			row.getThemeName(),
			row.getStoreId(),
			row.getStoreName(),
			toRegionLabel(row.getRegion(), row.getDistrict()),
			row.getGenre(),
			row.getPosterImageUrl(),
			row.getDifficulty(),
			row.getActivityLabel(),
			row.getRecommendedPlayers(),
			row.getRunningTimeMinutes(),
			row.getFavoriteCount()
		);
	}

	private ExploreThemeDetailView.RelatedTheme toRelatedThemeSummary(ThemeJpaRepository.RelatedThemeProjection row) {
		return ExploreThemeDetailView.RelatedTheme.of(
			row.getThemeId(),
			row.getThemeName(),
			row.getStoreId(),
			row.getStoreName(),
			toRegionLabel(row.getRegion(), row.getDistrict()),
			row.getGenre(),
			row.getPosterImageUrl(),
			row.getDifficulty(),
			row.getRunningTimeMinutes(),
			row.getFavoriteCount()
		);
	}

	private String toRegionLabel(String region, String district) {
		if (district == null || district.isBlank()) {
			return region;
		}
		return region + " " + district;
	}
}
