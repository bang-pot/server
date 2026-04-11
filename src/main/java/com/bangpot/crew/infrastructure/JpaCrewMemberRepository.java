package com.bangpot.crew.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewMemberRepository implements CrewMemberRepository {

	private final CrewMemberJpaRepository crewMemberJpaRepository;

	@Override
	public CrewMember save(CrewMember crewMember) {
		return crewMemberJpaRepository.save(crewMember);
	}

	@Override
	public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.existsByCrewIdAndUserId(crewId, userId);
	}

	@Override
	public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.existsByCrewIdAndUserIdAndRole(crewId, userId, CrewRole.LEADER);
	}

	@Override
	public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
		return crewMemberJpaRepository.findByCrewIdAndUserId(crewId, userId);
	}
}
