package com.bangpot.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.crew.domain.Crew;

public interface CrewRepository {

	boolean existsByName(String name);

	Crew save(Crew crew);

	Optional<Crew> findById(Long crewId);

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	default Optional<Crew> findAnyById(Long crewId) {
		return findById(crewId);
	}

	List<Crew> findPublicCrews();
}
