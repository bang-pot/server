package com.banglog.crew.application.usecase;

import com.banglog.crew.domain.ExploreCrewSort;
import com.banglog.crew.domain.view.ExploreCrewCardsView;

public interface GetExploreCrewCardsUseCase {

	ExploreCrewCardsView handle(Query query);

	record Query(
		int page,
		int size,
		String keyword,
		ExploreCrewSort sort
	) {

		public static Query of(int page, int size, String keyword, ExploreCrewSort sort) {
			return new Query(page, size, keyword, sort == null ? ExploreCrewSort.LATEST : sort);
		}
	}
}
