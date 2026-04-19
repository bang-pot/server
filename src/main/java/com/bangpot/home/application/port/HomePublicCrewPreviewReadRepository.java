package com.bangpot.home.application.port;

import java.util.List;

public interface HomePublicCrewPreviewReadRepository {

	List<Item> findPreviewItems(int limit);

	record Item(
		Long crewId,
		String crewName,
		String coverImageUrl,
		Long memberCount,
		boolean isPublic
	) {
		public static Item of(
			Long crewId,
			String crewName,
			String coverImageUrl,
			Long memberCount,
			boolean isPublic
		) {
			return new Item(crewId, crewName, coverImageUrl, memberCount, isPublic);
		}
	}
}
