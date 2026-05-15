package com.bangpot.explore.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.bangpot.explore.application.port.ExploreQueryRepository;
import com.bangpot.explore.application.service.ThemeSearchCachePolicy;

class ThemeSearchCachePolicyTest {

	private final ThemeSearchCachePolicy policy = new ThemeSearchCachePolicy();

	@Test
	void cachesFirstPagePublicThemeSearchWithoutKeyword() {
		ExploreQueryRepository.SearchCondition condition = ExploreQueryRepository.SearchCondition.of(
			null,
			List.of("HORROR"),
			"SEOUL",
			"GANGNAM",
			0,
			20
		);

		assertThat(policy.cacheable(condition)).isTrue();
	}

	@Test
	void doesNotCacheKeywordSearchAndDeepPage() {
		assertThat(policy.cacheable(ExploreQueryRepository.SearchCondition.of(
			"gangnam",
			List.of(),
			null,
			null,
			0,
			20
		))).isFalse();
		assertThat(policy.cacheable(ExploreQueryRepository.SearchCondition.of(
			null,
			List.of(),
			null,
			null,
			1,
			20
		))).isFalse();
	}

	@Test
	void doesNotCacheDistrictWithoutRegion() {
		ExploreQueryRepository.SearchCondition condition = ExploreQueryRepository.SearchCondition.of(
			null,
			List.of(),
			null,
			"GANGNAM",
			0,
			20
		);

		assertThat(policy.cacheable(condition)).isFalse();
	}

	@Test
	void normalizesBlankGenresToEmptyValue() {
		ExploreQueryRepository.SearchCondition condition = ExploreQueryRepository.SearchCondition.of(
			null,
			List.of(" ", ""),
			null,
			null,
			0,
			20
		);

		assertThat(policy.keyOf(condition)).contains("genres=-");
	}

	@Test
	void buildsStableKeyForSameGenreSet() {
		ExploreQueryRepository.SearchCondition first = ExploreQueryRepository.SearchCondition.of(
			null,
			List.of("MYSTERY", "HORROR"),
			"SEOUL",
			"GANGNAM",
			0,
			20
		);
		ExploreQueryRepository.SearchCondition second = ExploreQueryRepository.SearchCondition.of(
			null,
			List.of("HORROR", "MYSTERY"),
			"SEOUL",
			"GANGNAM",
			0,
			20
		);

		assertThat(policy.keyOf(first)).isEqualTo(policy.keyOf(second));
	}
}
