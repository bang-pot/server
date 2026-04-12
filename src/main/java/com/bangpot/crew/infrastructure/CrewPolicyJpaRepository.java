package com.bangpot.crew.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewPolicy;

interface CrewPolicyJpaRepository extends JpaRepository<CrewPolicy, Long> {

	List<CrewPolicy> findAllByCrewIdOrderByCreatedAtAscIdAsc(Long crewId);
}
