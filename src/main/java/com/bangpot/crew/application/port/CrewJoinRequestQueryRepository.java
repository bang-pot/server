package com.bangpot.crew.application.port;

import com.bangpot.crew.domain.view.MyPendingCrewsView;

public interface CrewJoinRequestQueryRepository {

	MyPendingCrewsView findMyPendingCrewsViewByUserId(Long userId, int page, int size);
}
