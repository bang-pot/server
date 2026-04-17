package com.bangpot.user.application.usecase;

import java.util.List;

public interface GetMyCrewsUseCase {

	Result handle(Query query);

	record Query(Long userId, int page, int size) {
		public static Query of(Long userId, int page, int size) {
			return new Query(userId, page, size);
		}
	}

	record Result(
		List<Item> items,
		PageInfo pageInfo
	) {
		public static Result of(List<Item> items, PageInfo pageInfo) {
			return new Result(items, pageInfo);
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
