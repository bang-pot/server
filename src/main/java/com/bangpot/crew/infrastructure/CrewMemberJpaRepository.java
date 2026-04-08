package com.bangpot.crew.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.crew.domain.CrewMember;

interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

	boolean existsByCrewIdAndUserId(Long crewId, Long userId);
}
