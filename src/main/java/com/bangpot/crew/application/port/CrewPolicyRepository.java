package com.bangpot.crew.application.port;

import java.util.List;

import com.bangpot.crew.domain.CrewPolicy;

public interface CrewPolicyRepository {

	CrewPolicy save(CrewPolicy crewPolicy);

	List<CrewPolicy> findAllByCrewId(Long crewId);
}
