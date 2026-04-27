package com.bangpot.crew.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewJoinRequestQueryRepository;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.bangpot.crew.domain.view.CrewJoinRequestsView;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.MyPendingCrewsView;
import com.bangpot.crew.domain.view.PendingCrewJoinRequestsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaCrewJoinRequestQueryRepository implements CrewJoinRequestQueryRepository {

	private final CrewJoinRequestJpaRepository crewJoinRequestJpaRepository;

	@Override
	public Optional<CrewJoinRequestManagementAccessView> findManagementAccessByCrewIdAndUserId(
		Long crewId,
		Long userId
	) {
		return crewJoinRequestJpaRepository.findManagementAccessByCrewIdAndUserId(
			crewId,
			userId,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE
		);
	}

	@Override
	public CrewJoinRequestsView findCrewJoinRequestsViewByCrewId(Long crewId, int page, int size) {
		Slice<CrewJoinRequestsView.Item> slice = crewJoinRequestJpaRepository.findCrewJoinRequestItemsByCrewId(
			crewId,
			CrewJoinRequestStatus.PENDING,
			CrewJoinRequestStatus.APPROVED,
			CrewJoinRequestStatus.REJECTED,
			PageRequest.of(page, size)
		);
		return CrewJoinRequestsView.of(
			slice.getContent(),
			CrewJoinRequestsView.Page.of(page, size, slice.hasNext())
		);
	}

	@Override
	public PendingCrewJoinRequestsView findPendingCrewJoinRequestsViewByCrewId(Long crewId, int page, int size) {
		Slice<PendingCrewJoinRequestsView.Item> slice =
			crewJoinRequestJpaRepository.findPendingCrewJoinRequestItemsByCrewId(
				crewId,
				CrewJoinRequestStatus.PENDING,
				PageRequest.of(page, size)
			);
		return PendingCrewJoinRequestsView.of(
			slice.getContent(),
			PendingCrewJoinRequestsView.Page.of(page, size, slice.hasNext())
		);
	}

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
