package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.domain.view.CrewPoliciesView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewPoliciesService implements GetCrewPoliciesUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입한 크루원만 크루 정책을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewPoliciesView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		CrewPoliciesView view = crewQueryRepository.findCrewPoliciesViewByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (view.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		return view;
	}
}
