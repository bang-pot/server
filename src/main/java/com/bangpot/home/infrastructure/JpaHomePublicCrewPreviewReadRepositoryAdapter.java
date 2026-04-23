package com.bangpot.home.infrastructure;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.home.application.port.HomePublicCrewPreviewReadRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaHomePublicCrewPreviewReadRepositoryAdapter implements HomePublicCrewPreviewReadRepository {

	private final JpaHomePublicCrewPreviewReadRepository repository;

	@Override
	public java.util.List<Item> findPreviewItems(int limit) {
		return repository.findRows(
			CrewStatus.ACTIVE,
			CrewVisibility.PUBLIC,
			CrewMemberStatus.ACTIVE,
			PageRequest.of(0, limit)
		).stream()
			.map(row -> Item.of(
				row.getCrewId(),
				row.getCrewName(),
				row.getCoverImageUrl(),
				row.getMemberCount(),
				true
			))
			.toList();
	}
}
