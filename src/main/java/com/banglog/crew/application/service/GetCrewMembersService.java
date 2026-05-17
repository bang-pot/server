package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.application.usecase.GetCrewMembersUseCase;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMembersService implements GetCrewMembersUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "가입한 크루원만 크루원 목록을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewMembersView handle(Query query) {
		CrewMembersView view = crewQueryRepository.findCrewMembersViewByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		if (view.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		return view;
	}
}
