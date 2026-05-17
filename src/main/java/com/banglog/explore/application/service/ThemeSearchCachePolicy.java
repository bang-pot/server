package com.banglog.explore.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.banglog.explore.application.port.ExploreQueryRepository;

@Component
public class ThemeSearchCachePolicy {

	private static final int CACHEABLE_PAGE = 0;
	private static final int MAX_CACHEABLE_SIZE = 20;
	private static final String EMPTY_VALUE = "-";

	public boolean cacheable(ExploreQueryRepository.SearchCondition searchCondition) {
		return searchCondition.page() == CACHEABLE_PAGE
			&& searchCondition.size() > 0
			&& searchCondition.size() <= MAX_CACHEABLE_SIZE
			&& isBlank(searchCondition.keyword())
			&& hasValidLocation(searchCondition);
	}

	public String keyOf(ExploreQueryRepository.SearchCondition searchCondition) {
		return String.join(
			":",
			"v1",
			"page=" + searchCondition.page(),
			"size=" + searchCondition.size(),
			"genres=" + normalizedGenres(searchCondition.genres()),
			"region=" + normalizedText(searchCondition.region()),
			"district=" + normalizedText(searchCondition.district())
		);
	}

	private String normalizedGenres(List<String> genres) {
		if (genres == null || genres.isEmpty()) {
			return EMPTY_VALUE;
		}
		String normalized = genres.stream()
			.filter(genre -> !isBlank(genre))
			.map(String::trim)
			.sorted()
			.collect(Collectors.joining(","));
		if (normalized.isBlank()) {
			return EMPTY_VALUE;
		}
		return normalized;
	}

	private String normalizedText(String value) {
		if (isBlank(value)) {
			return EMPTY_VALUE;
		}
		return value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private boolean hasValidLocation(ExploreQueryRepository.SearchCondition searchCondition) {
		return isBlank(searchCondition.district()) || !isBlank(searchCondition.region());
	}
}
