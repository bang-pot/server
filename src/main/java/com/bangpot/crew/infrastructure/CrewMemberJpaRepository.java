package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;

interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

	boolean existsByCrewIdAndUserId(Long crewId, Long userId);

	boolean existsByCrewIdAndUserIdAndRole(Long crewId, Long userId, CrewRole role);

	Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

	List<CrewMember> findAllByCrewId(Long crewId);

	void deleteByCrewIdAndUserId(Long crewId, Long userId);
}
