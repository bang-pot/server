package com.banglog.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.port.CrewJoinRequestQueryRepository;
import com.banglog.crew.domain.view.MyPendingCrewsView;
import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.application.usecase.GetMyPendingCrewsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyPendingCrewsService implements GetMyPendingCrewsUseCase {

	private final UserQueryRepository userQueryRepository;
	private final CrewJoinRequestQueryRepository crewJoinRequestQueryRepository;

	@Override
	public MyPendingCrewsView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		return crewJoinRequestQueryRepository.findMyPendingCrewsViewByUserId(
			query.userId(),
			query.page(),
			query.size()
		);
	}
}
