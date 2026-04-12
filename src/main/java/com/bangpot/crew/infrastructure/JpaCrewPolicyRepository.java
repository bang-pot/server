package com.bangpot.crew.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewPolicyRepository;
import com.bangpot.crew.domain.CrewPolicy;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewPolicyRepository implements CrewPolicyRepository {

	private final CrewPolicyJpaRepository crewPolicyJpaRepository;

	@Override
	public CrewPolicy save(CrewPolicy crewPolicy) {
		return crewPolicyJpaRepository.save(crewPolicy);
	}

	@Override
	public List<CrewPolicy> findAllByCrewId(Long crewId) {
		return crewPolicyJpaRepository.findAllByCrewIdOrderByCreatedAtAscIdAsc(crewId);
	}
}
