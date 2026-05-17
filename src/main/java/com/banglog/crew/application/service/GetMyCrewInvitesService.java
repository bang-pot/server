package com.banglog.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.port.CrewInviteQueryRepository;
import com.banglog.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.banglog.crew.domain.view.MyCrewInvitesView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyCrewInvitesService implements GetMyCrewInvitesUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewInviteQueryRepository crewInviteQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MyCrewInvitesView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "초대 목록을 조회할 수 없습니다.");
		return crewInviteQueryRepository.findMyCrewInvitesViewByTargetUserId(
			query.userId(),
			query.page(),
			query.size()
		);
	}
}
