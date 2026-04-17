package com.bangpot.user.application.port;

import java.util.List;

public interface MyCrewReadRepository {

	SearchResult search(Long userId, int page, int size);

	record SearchResult(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static SearchResult of(List<Item> items, PageInfo pageInfo) {
			return new SearchResult(items, pageInfo);
		}
	}

	record Item(
		Long crewId,
		String crewName,
		String visibility,
		String leaderNickname,
		String coverImageUrl
	) {
		public static Item of(
			Long crewId,
			String crewName,
			String visibility,
			String leaderNickname,
			String coverImageUrl
		) {
			return new Item(crewId, crewName, visibility, leaderNickname, coverImageUrl);
		}
	}

	record PageInfo(
		int page,
		int size,
		boolean hasNext
	) {
		public static PageInfo of(int page, int size, boolean hasNext) {
			return new PageInfo(page, size, hasNext);
		}
	}
}
