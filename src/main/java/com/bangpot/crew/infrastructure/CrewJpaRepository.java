package com.bangpot.crew.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;

interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	boolean existsByName(String name);

	java.util.Optional<Crew> findByIdAndStatus(Long id, CrewStatus status);

	List<Crew> findAllByStatusAndVisibilityOrderByIdAsc(CrewStatus status, CrewVisibility visibility);
}
