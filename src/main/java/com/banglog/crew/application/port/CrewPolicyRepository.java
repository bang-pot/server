package com.banglog.crew.application.port;

import java.util.List;

import com.banglog.crew.domain.CrewPolicy;

public interface CrewPolicyRepository {

	CrewPolicy save(CrewPolicy crewPolicy);

	List<CrewPolicy> findAllByCrewId(Long crewId);
}
