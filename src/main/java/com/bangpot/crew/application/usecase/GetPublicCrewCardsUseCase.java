package com.bangpot.crew.application.usecase;

import com.bangpot.crew.domain.view.PublicCrewCardsView;

public interface GetPublicCrewCardsUseCase {

	PublicCrewCardsView handle(Query query);

	record Query(
		int page,
		int size
	) {

		public static Query of(int page, int size) {
			return new Query(page, size);
		}
	}
}
