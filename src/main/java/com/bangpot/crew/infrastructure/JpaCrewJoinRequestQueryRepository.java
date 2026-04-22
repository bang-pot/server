package com.bangpot.crew.infrastructure;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewJoinRequestQueryRepository;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyPendingCrewsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaCrewJoinRequestQueryRepository implements CrewJoinRequestQueryRepository {

	private final CrewJoinRequestJpaRepository crewJoinRequestJpaRepository;

	@Override
	public MyPendingCrewsView findMyPendingCrewsViewByUserId(Long userId, int page, int size) {
		Slice<CrewJoinRequestJpaRepository.MyPendingCrewRow> slice =
			crewJoinRequestJpaRepository.findMyPendingCrewsViewByUserId(
				userId,
				CrewJoinRequestStatus.PENDING,
				CrewStatus.ACTIVE,
				CrewVisibility.PUBLIC,
				PageRequest.of(page, size)
			);

		return MyPendingCrewsView.of(
			slice.getContent().stream()
				.map(row -> MyPendingCrewsView.Item.of(
					row.getJoinRequestId(),
					row.getCrewId(),
					row.getCrewName(),
					row.getRequestedAt().toString(),
					summarizeMessage(row.getMessage())
				))
				.toList(),
			MyPendingCrewsView.Page.of(page, size, slice.hasNext())
		);
	}

	private String summarizeMessage(String message) {
		if (message == null) {
			return null;
		}
		String normalized = message.replaceAll("\\s+", " ").trim();
		if (normalized.isBlank()) {
			return null;
		}
		if (normalized.length() <= 60) {
			return normalized;
		}
		return normalized.substring(0, 60);
	}
}
