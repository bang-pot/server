package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.user.application.port.MyCrewReadRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMyCrewReadRepositoryAdapter implements MyCrewReadRepository {

	private final JpaMyCrewReadRepository repository;

	@Override
	public SearchResult search(Long userId, int page, int size) {
		List<JpaMyCrewReadRepository.Row> rows = repository.searchRows(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE,
			CrewRole.LEADER,
			PageRequest.of(page, size + 1)
		);
		boolean hasNext = rows.size() > size;
		List<JpaMyCrewReadRepository.Row> pageRows = hasNext ? rows.subList(0, size) : rows;
		return SearchResult.of(
			pageRows.stream()
				.map(row -> Item.of(
					row.getCrewId(),
					row.getCrewName(),
					row.getVisibility(),
					row.getLeaderNickname(),
					row.getCoverImageUrl()
				))
				.toList(),
			PageInfo.of(page, size, hasNext)
		);
	}
}
