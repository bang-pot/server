package com.bangpot.crew.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.Crew;

interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	boolean existsByName(String name);
}
