package com.bangpot.crew.application.port;

import com.bangpot.crew.domain.view.MyCrewInvitesView;

public interface CrewInviteQueryRepository {

	MyCrewInvitesView findMyCrewInvitesViewByTargetUserId(Long targetUserId, int page, int size);
}
