package com.bangpot.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.crew.domain.CrewJoinRequest;
public interface CrewJoinRequestRepository {

	CrewJoinRequest save(CrewJoinRequest crewJoinRequest);

	boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId);

	List<CrewJoinRequest> findByCrewId(Long crewId);

	List<CrewJoinRequest> findPendingByCrewId(Long crewId);

	Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId);

	Optional<CrewJoinRequest> findPendingByIdAndCrewIdForUpdate(Long requestId, Long crewId);

	Optional<CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId);

	Optional<CrewJoinRequest> findPendingByIdAndUserIdForUpdate(Long requestId, Long userId);
}
