package com.banglog.crew.application.port;

import java.util.Optional;

import com.banglog.crew.domain.CrewInvite;

public interface CrewInviteRepository {

	CrewInvite save(CrewInvite invite);

	boolean existsPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId);

	Optional<CrewInvite> findPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId);

	Optional<CrewInvite> findPendingByIdAndTargetUserId(Long inviteId, Long targetUserId);

	Optional<CrewInvite> findPendingByIdAndTargetUserIdForUpdate(Long inviteId, Long targetUserId);

	Optional<CrewInvite> findById(Long inviteId);
}
