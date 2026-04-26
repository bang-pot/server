package com.bangpot.crew.application.port;

import java.util.Optional;

import com.bangpot.crew.domain.CrewInvite;

public interface CrewInviteRepository {

	CrewInvite save(CrewInvite invite);

	boolean existsPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId);

	Optional<CrewInvite> findPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId);

	Optional<CrewInvite> findPendingByIdAndTargetUserId(Long inviteId, Long targetUserId);

	Optional<CrewInvite> findById(Long inviteId);
}
