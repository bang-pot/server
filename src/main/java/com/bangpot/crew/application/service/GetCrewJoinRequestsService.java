package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestQueryRepository;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.bangpot.crew.domain.view.CrewJoinRequestsView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewJoinRequestsService implements GetCrewJoinRequestsUseCase {

	private final CrewJoinRequestQueryRepository crewJoinRequestQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewJoinRequestsView handle(Query query) {
		CrewJoinRequestManagementAccessView access = crewJoinRequestQueryRepository
			.findManagementAccessByCrewIdAndUserId(query.crewId(), query.leaderUserId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		if (access.myRole() != CrewRole.LEADER) {
			throw new AccessDeniedException("가입 신청 관리 권한이 없습니다.");
		}
		return crewJoinRequestQueryRepository.findCrewJoinRequestsViewByCrewId(
			query.crewId(),
			query.page(),
			query.size()
		);
	}
}
