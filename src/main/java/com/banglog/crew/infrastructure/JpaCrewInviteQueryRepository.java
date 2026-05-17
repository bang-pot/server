package com.banglog.crew.infrastructure;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.banglog.crew.application.port.CrewInviteQueryRepository;
import com.banglog.crew.domain.CrewStatus;
import com.banglog.crew.domain.view.MyCrewInvitesView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaCrewInviteQueryRepository implements CrewInviteQueryRepository {

	private final CrewInviteJpaRepository crewInviteJpaRepository;

	@Override
	public MyCrewInvitesView findMyCrewInvitesViewByTargetUserId(Long targetUserId, int page, int size) {
		Slice<MyCrewInvitesView.Item> result = crewInviteJpaRepository.findMyCrewInviteItemsByTargetUserId(
			targetUserId,
			CrewStatus.ACTIVE,
			PageRequest.of(page, size)
		);
		return MyCrewInvitesView.of(
			result.getContent(),
			MyCrewInvitesView.Page.of(page, size, result.hasNext())
		);
	}
}
