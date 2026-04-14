package com.bangpot.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.crew.domain.CrewMember;

public interface CrewMemberRepository {

	CrewMember save(CrewMember crewMember);

	boolean existsByCrewIdAndUserId(Long crewId, Long userId);

	boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

	default Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
		return findByCrewIdAndUserId(crewId, userId);
	}

	List<CrewMember> findAllByCrewId(Long crewId);

	default List<CrewMember> findAllByUserId(Long userId) {
		throw new UnsupportedOperationException();
	}
}
