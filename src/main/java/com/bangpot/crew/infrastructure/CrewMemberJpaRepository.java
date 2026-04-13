package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewMemberStatus;

interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

	boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewMemberStatus status);

	boolean existsByCrewIdAndUserIdAndRoleAndStatus(Long crewId, Long userId, CrewRole role, CrewMemberStatus status);

	Optional<CrewMember> findByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewMemberStatus status);

	Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

	List<CrewMember> findAllByCrewIdAndStatus(Long crewId, CrewMemberStatus status);
}
