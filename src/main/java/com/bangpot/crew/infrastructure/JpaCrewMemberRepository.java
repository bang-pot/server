package com.bangpot.crew.infrastructure;

import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.domain.CrewMember;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaCrewMemberRepository implements CrewMemberRepository {

	private final CrewMemberJpaRepository crewMemberJpaRepository;

	@Override
	public CrewMember save(CrewMember crewMember) {
		return crewMemberJpaRepository.save(crewMember);
	}
}
