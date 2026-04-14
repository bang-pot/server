package com.bangpot.explore.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.explore.application.usecase.GetExploreFiltersUseCase;
import com.bangpot.explore.application.usecase.GetExploreThemesUseCase;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaExploreThemeReadRepository implements ExploreThemeReadRepository {

	private static final String UNUSED_GENRE = "__UNUSED__";

	private final ThemeJpaRepository themeJpaRepository;
	private final StoreJpaRepository storeJpaRepository;

	@Override
	public SearchResult search(Condition condition) {
		List<String> genres = normalizedGenres(condition.genres());
		boolean keywordEmpty = isBlank(condition.keyword());
		boolean regionEmpty = isBlank(condition.region());
		boolean districtEmpty = isBlank(condition.district());
		Slice<ThemeJpaRepository.ThemeCardProjection> slice = themeJpaRepository.search(
			keywordEmpty,
			keywordPattern(condition.keyword()),
			genres,
			condition.genres() == null || condition.genres().isEmpty(),
			regionEmpty,
			normalizedText(condition.region()),
			districtEmpty,
			normalizedText(condition.district()),
			PageRequest.of(condition.page(), condition.size())
		);

		List<GetExploreThemesUseCase.Item> items = slice.getContent().stream()
			.map(this::toItem)
			.toList();

		return SearchResult.of(
			items,
			GetExploreThemesUseCase.PageInfo.of(condition.page(), condition.size(), slice.hasNext())
		);
	}

	@Override
	public GetExploreFiltersUseCase.Result getFilters() {
		List<String> genres = themeJpaRepository.findActiveGenres();
		List<StoreJpaRepository.RegionDistrictProjection> regionRows = storeJpaRepository.findActiveRegionDistricts();

		java.util.Map<String, java.util.List<String>> grouped = new java.util.LinkedHashMap<>();
		for (StoreJpaRepository.RegionDistrictProjection row : regionRows) {
			String region = row.getRegion();
			String district = row.getDistrict();
			grouped.computeIfAbsent(region, key -> new java.util.ArrayList<>());
			if (district != null && !district.isBlank() && !grouped.get(region).contains(district)) {
				grouped.get(region).add(district);
			}
		}

		List<GetExploreFiltersUseCase.RegionOption> regions = grouped.entrySet().stream()
			.map(entry -> GetExploreFiltersUseCase.RegionOption.of(entry.getKey(), List.copyOf(entry.getValue())))
			.toList();

		return GetExploreFiltersUseCase.Result.of(genres, regions);
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

	private GetExploreThemesUseCase.Item toItem(ThemeJpaRepository.ThemeCardProjection row) {
		return GetExploreThemesUseCase.Item.of(
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
			row.getFavoriteCount(),
			false
		);
	}

	private String toRegionLabel(String region, String district) {
		if (district == null || district.isBlank()) {
			return region;
		}
		return region + " " + district;
	}
}
