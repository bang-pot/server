package com.banglog.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.banglog.crew.domain.CrewMember;

public interface CrewMemberRepository {

	CrewMember save(CrewMember crewMember);

	boolean existsByCrewIdAndUserId(Long crewId, Long userId);

	boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId);

	boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId);

	Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

	Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId);

	List<CrewMember> findAllByCrewId(Long crewId);

	List<CrewMember> findAllByUserId(Long userId);
}
