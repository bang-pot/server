package com.bangpot.explore.infrastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

	private final ThemeJpaRepository themeJpaRepository;
	private final StoreJpaRepository storeJpaRepository;
	private final ThemeFavoriteJpaRepository themeFavoriteJpaRepository;

	@Override
	public ExploreThemeSearchView search(SearchCondition searchCondition) {
		boolean genresEmpty = searchCondition.genres() == null || searchCondition.genres().isEmpty();
		List<Long> genreThemeIds = findThemeIdsByGenres(searchCondition.genres());
		if (!genresEmpty && genreThemeIds.isEmpty()) {
			return ExploreThemeSearchView.of(
				List.of(),
				ExploreThemeSearchView.PageInfo.of(searchCondition.page(), searchCondition.size(), false, 0, 0)
			);
		}

		boolean keywordEmpty = isBlank(searchCondition.keyword());
		boolean regionEmpty = isBlank(searchCondition.region());
		boolean districtEmpty = isBlank(searchCondition.district());
		Page<ThemeJpaRepository.ThemeCardProjection> page = themeJpaRepository.search(
			keywordEmpty,
			keywordPattern(searchCondition.keyword()),
			genreThemeIds,
			genresEmpty,
			regionEmpty,
			normalizedText(searchCondition.region()),
			districtEmpty,
			normalizedText(searchCondition.district()),
			PageRequest.of(searchCondition.page(), searchCondition.size())
		);

		Map<Long, List<String>> genresByThemeId = findGenresByThemeIds(
			page.getContent().stream()
				.map(ThemeJpaRepository.ThemeCardProjection::getThemeId)
				.toList()
		);
		List<ExploreThemeSearchView.Item> items = page.getContent().stream()
			.map(row -> toItem(row, genresByThemeId.getOrDefault(row.getThemeId(), List.of())))
			.toList();

		return ExploreThemeSearchView.of(
			items,
			ExploreThemeSearchView.PageInfo.of(
				searchCondition.page(),
				searchCondition.size(),
				page.hasNext(),
				page.getTotalElements(),
				page.getTotalPages()
			)
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
			.map(row -> {
				List<ThemeJpaRepository.RelatedThemeProjection> relatedRows = themeJpaRepository.findRelatedActiveThemes(
					row.getStoreId(),
					row.getThemeId(),
					PageRequest.of(0, 4)
				);
				List<Long> themeIds = new ArrayList<>(relatedRows.size() + 1);
				themeIds.add(row.getThemeId());
				themeIds.addAll(relatedRows.stream()
					.map(ThemeJpaRepository.RelatedThemeProjection::getThemeId)
					.toList());
				Map<Long, List<String>> genresByThemeId = findGenresByThemeIds(themeIds);

				return ExploreThemeDetailView.of(
					row.getThemeId(),
					row.getThemeName(),
					row.getStoreId(),
					row.getStoreName(),
					toRegionLabel(row.getRegion(), row.getDistrict()),
					genresByThemeId.getOrDefault(row.getThemeId(), List.of()),
					row.getPosterImageUrl(),
					row.getDifficulty(),
					row.getRunningTimeMinutes(),
					row.getDescription(),
					row.getExternalLink(),
					relatedRows.stream()
						.map(relatedTheme -> toRelatedThemeSummary(
							relatedTheme,
							genresByThemeId.getOrDefault(relatedTheme.getThemeId(), List.of())
						))
						.toList()
				);
			});
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

	private List<Long> findThemeIdsByGenres(List<String> genres) {
		if (genres == null || genres.isEmpty()) {
			return List.of();
		}
		return themeJpaRepository.findThemeIdsByGenres(genres);
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

	private ExploreThemeSearchView.Item toItem(ThemeJpaRepository.ThemeCardProjection row, List<String> genres) {
		return ExploreThemeSearchView.Item.of(
			row.getThemeId(),
			row.getThemeName(),
			row.getStoreId(),
			row.getStoreName(),
			toRegionLabel(row.getRegion(), row.getDistrict()),
			genres,
			row.getPosterImageUrl(),
			row.getDifficulty(),
			row.getActivityLabel(),
			row.getRecommendedPlayers(),
			row.getRunningTimeMinutes(),
			row.getFavoriteCount()
		);
	}

	private ExploreThemeDetailView.RelatedTheme toRelatedThemeSummary(
		ThemeJpaRepository.RelatedThemeProjection row,
		List<String> genres
	) {
		return ExploreThemeDetailView.RelatedTheme.of(
			row.getThemeId(),
			row.getThemeName(),
			row.getStoreId(),
			row.getStoreName(),
			toRegionLabel(row.getRegion(), row.getDistrict()),
			genres,
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

	private Map<Long, List<String>> findGenresByThemeIds(List<Long> themeIds) {
		if (themeIds == null || themeIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, List<String>> genresByThemeId = new LinkedHashMap<>();
		for (ThemeJpaRepository.ThemeGenreProjection row : themeJpaRepository.findGenresByThemeIds(themeIds)) {
			genresByThemeId.computeIfAbsent(row.getThemeId(), ignored -> new ArrayList<>())
				.add(row.getGenreName());
		}
		return genresByThemeId;
	}
}
