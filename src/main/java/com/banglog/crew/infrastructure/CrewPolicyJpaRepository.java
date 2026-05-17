package com.banglog.crew.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banglog.crew.domain.CrewPolicy;

interface CrewPolicyJpaRepository extends JpaRepository<CrewPolicy, Long> {

	List<CrewPolicy> findAllByCrewIdOrderByCreatedAtAscIdAsc(Long crewId);
}
