package com.bangpot.crew.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewRepository implements CrewRepository {

	private final CrewJpaRepository crewJpaRepository;

	@Override
	public boolean existsByName(String name) {
		return crewJpaRepository.existsByName(name);
	}

	@Override
	public Crew save(Crew crew) {
		return crewJpaRepository.save(crew);
	}

	@Override
	public Optional<Crew> findById(Long crewId) {
		return crewJpaRepository.findById(crewId);
	}
}
