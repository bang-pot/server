package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestQueryRepository;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.bangpot.crew.domain.view.PendingCrewJoinRequestsView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPendingCrewJoinRequestsService implements GetPendingCrewJoinRequestsUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입 신청 관리 권한이 없습니다.";

	private final CrewJoinRequestQueryRepository crewJoinRequestQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public PendingCrewJoinRequestsView handle(Query query) {
		CrewJoinRequestManagementAccessView access = crewJoinRequestQueryRepository
			.findManagementAccessByCrewIdAndUserId(query.crewId(), query.leaderUserId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		if (access.myRole() != CrewRole.LEADER) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}
		return crewJoinRequestQueryRepository.findPendingCrewJoinRequestsViewByCrewId(
			query.crewId(),
			query.page(),
			query.size()
		);
	}
}
