package com.bangpot.crew.application.port;

import com.bangpot.crew.domain.CrewJoinRequest;

public interface CrewJoinRequestRepository {

	CrewJoinRequest save(CrewJoinRequest crewJoinRequest);

	boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId);
}
