package com.banglog.crew.application.port;

import java.util.Optional;

import com.banglog.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.banglog.crew.domain.view.CrewJoinRequestsView;
import com.banglog.crew.domain.view.MyPendingCrewsView;
import com.banglog.crew.domain.view.PendingCrewJoinRequestsView;

public interface CrewJoinRequestQueryRepository {

	Optional<CrewJoinRequestManagementAccessView> findManagementAccessByCrewIdAndUserId(Long crewId, Long userId);

	CrewJoinRequestsView findCrewJoinRequestsViewByCrewId(Long crewId, int page, int size);

	PendingCrewJoinRequestsView findPendingCrewJoinRequestsViewByCrewId(Long crewId, int page, int size);

	MyPendingCrewsView findMyPendingCrewsViewByUserId(Long userId, int page, int size);
}
