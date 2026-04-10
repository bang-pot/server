package com.bangpot.crew.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewInviteRepository implements CrewInviteRepository {

	private final CrewInviteJpaRepository crewInviteJpaRepository;

	@Override
	public CrewInvite save(CrewInvite invite) {
		return crewInviteJpaRepository.save(invite);
	}

	@Override
	public boolean existsPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId) {
		return crewInviteJpaRepository.existsByCrewIdAndTargetUserIdAndStatus(
			crewId,
			targetUserId,
			CrewInviteStatus.PENDING
		);
	}

	@Override
	public Optional<CrewInvite> findPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId) {
		return crewInviteJpaRepository.findByCrewIdAndTargetUserIdAndStatus(
			crewId,
			targetUserId,
			CrewInviteStatus.PENDING
		);
	}
}
