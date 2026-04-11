package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;

interface CrewInviteJpaRepository extends JpaRepository<CrewInvite, Long> {

	boolean existsByCrewIdAndTargetUserIdAndStatus(Long crewId, Long targetUserId, CrewInviteStatus status);

	Optional<CrewInvite> findByCrewIdAndTargetUserIdAndStatus(Long crewId, Long targetUserId, CrewInviteStatus status);

	Optional<CrewInvite> findByIdAndTargetUserIdAndStatus(Long inviteId, Long targetUserId, CrewInviteStatus status);

	List<CrewInvite> findByTargetUserIdOrderByIdDesc(Long targetUserId);
}
