package com.banglog.crew.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.banglog.crew.application.port.CrewPolicyRepository;
import com.banglog.crew.domain.CrewPolicy;

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
