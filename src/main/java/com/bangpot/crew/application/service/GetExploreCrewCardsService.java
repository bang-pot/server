package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.usecase.GetExploreCrewCardsUseCase;
import com.bangpot.crew.domain.view.ExploreCrewCardsView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExploreCrewCardsService implements GetExploreCrewCardsUseCase {

	private static final int MIN_PAGE = 0;
	private static final int MIN_SIZE = 1;
	private static final int MAX_SIZE = 50;

	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public ExploreCrewCardsView handle(Query query) {
		int page = Math.max(query.page(), MIN_PAGE);
		int size = Math.max(MIN_SIZE, Math.min(query.size(), MAX_SIZE));
		String keyword = normalizeKeyword(query.keyword());
		return crewQueryRepository.findExploreCrewCardsView(keyword, query.sort(), page, size);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String normalizedKeyword = keyword.trim();
		return normalizedKeyword.isEmpty() ? null : normalizedKeyword;
	}
}
