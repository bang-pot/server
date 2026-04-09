package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;

interface CrewJoinRequestJpaRepository extends JpaRepository<CrewJoinRequest, Long> {

	boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewJoinRequestStatus status);

	List<CrewJoinRequest> findAllByCrewIdAndStatusOrderByIdAsc(Long crewId, CrewJoinRequestStatus status);

	Optional<CrewJoinRequest> findByIdAndCrewIdAndStatus(Long id, Long crewId, CrewJoinRequestStatus status);
}
