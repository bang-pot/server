package com.bangpot.crew.domain.view;

import java.util.List;

import com.bangpot.crew.domain.CrewRole;

public record CrewPoliciesView(
	CrewRole myRole,
	List<CrewPoliciesView.Item> items
) {

	public static CrewPoliciesView of(CrewRole myRole, List<CrewPoliciesView.Item> items) {
		return new CrewPoliciesView(myRole, items);
	}

	public record Item(
		Long policyId,
		String title,
		String content
	) {

		public static Item of(Long policyId, String title, String content) {
			return new Item(policyId, title, content);
		}
	}
}
