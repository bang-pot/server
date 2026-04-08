package com.bangpot.crew.infrastructure;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewJoinRequestRepository implements CrewJoinRequestRepository {

	private final CrewJoinRequestJpaRepository crewJoinRequestJpaRepository;

	@Override
	public CrewJoinRequest save(CrewJoinRequest crewJoinRequest) {
		return crewJoinRequestJpaRepository.save(crewJoinRequest);
	}

	@Override
	public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
		return crewJoinRequestJpaRepository.existsByCrewIdAndUserIdAndStatus(
			crewId,
			userId,
			CrewJoinRequestStatus.PENDING
		);
	}
}
