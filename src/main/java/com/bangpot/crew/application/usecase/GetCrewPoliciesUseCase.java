package com.bangpot.crew.application.usecase;

import java.util.List;

public interface GetCrewPoliciesUseCase {

	List<View> handle(Query query);

	record Query(Long crewId, Long userId) {

		public static Query of(Long crewId, Long userId) {
			return new Query(crewId, userId);
		}
	}

	record View(Long policyId, String title, String content) {

		public static View of(Long policyId, String title, String content) {
			return new View(policyId, title, content);
		}
	}
}
