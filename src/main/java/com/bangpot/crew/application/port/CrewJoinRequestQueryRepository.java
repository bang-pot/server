package com.bangpot.crew.application.port;

import java.util.Optional;

import com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.bangpot.crew.domain.view.CrewJoinRequestsView;
import com.bangpot.crew.domain.view.MyPendingCrewsView;

public interface CrewJoinRequestQueryRepository {

	Optional<CrewJoinRequestManagementAccessView> findManagementAccessByCrewIdAndUserId(Long crewId, Long userId);

	CrewJoinRequestsView findCrewJoinRequestsViewByCrewId(Long crewId, int page, int size);

	MyPendingCrewsView findMyPendingCrewsViewByUserId(Long userId, int page, int size);
}
