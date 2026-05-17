package com.banglog.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewMemberStatus;

interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

	boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewMemberStatus status);

	boolean existsByCrewIdAndUserIdAndRoleAndStatus(Long crewId, Long userId, CrewRole role, CrewMemberStatus status);

	boolean existsByCrewIdAndUserIdNotAndStatus(Long crewId, Long userId, CrewMemberStatus status);

	Optional<CrewMember> findByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewMemberStatus status);

	Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

	List<CrewMember> findAllByCrewIdAndStatus(Long crewId, CrewMemberStatus status);

	List<CrewMember> findAllByUserIdAndStatusOrderByCrewIdAsc(Long userId, CrewMemberStatus status);
}
