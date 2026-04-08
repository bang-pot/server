package com.bangpot.crew.application.port;

import com.bangpot.crew.domain.CrewMember;

public interface CrewMemberRepository {

	CrewMember save(CrewMember crewMember);

	boolean existsByCrewIdAndUserId(Long crewId, Long userId);
}
