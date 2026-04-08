package com.bangpot.crew.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;

interface CrewJoinRequestJpaRepository extends JpaRepository<CrewJoinRequest, Long> {

	boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewJoinRequestStatus status);
}
