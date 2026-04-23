package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

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

	@Override
	public List<CrewJoinRequest> findByCrewId(Long crewId) {
		return crewJoinRequestJpaRepository.findAllByCrewIdOrderByIdAsc(crewId);
	}

	@Override
	public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
		return crewJoinRequestJpaRepository.findAllByCrewIdAndStatusOrderByIdAsc(
			crewId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
		return crewJoinRequestJpaRepository.findByIdAndCrewIdAndStatus(
			requestId,
			crewId,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
		return crewJoinRequestJpaRepository.findByIdAndUserIdAndStatus(
			requestId,
			userId,
			CrewJoinRequestStatus.PENDING
		);
	}
}
