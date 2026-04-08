package com.bangpot.crew.application.port;

import java.util.Optional;

import com.bangpot.crew.domain.Crew;

public interface CrewRepository {

	boolean existsByName(String name);

	Crew save(Crew crew);

	Optional<Crew> findById(Long crewId);
}
