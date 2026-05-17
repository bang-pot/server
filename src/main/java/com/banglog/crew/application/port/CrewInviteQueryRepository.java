package com.banglog.crew.application.port;

import com.banglog.crew.domain.view.MyCrewInvitesView;

public interface CrewInviteQueryRepository {

	MyCrewInvitesView findMyCrewInvitesViewByTargetUserId(Long targetUserId, int page, int size);
}
