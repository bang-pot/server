package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyCrewsService implements GetMyCrewsUseCase {

	private final UserQueryRepository userQueryRepository;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	public MyCrewsView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		return crewQueryRepository.findMyCrewsViewByMemberUserId(query.userId(), query.page(), query.size());
	}
}
