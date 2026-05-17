package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.application.usecase.GetCrewHubUseCase;
import com.banglog.crew.domain.view.CrewHubView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewHubService implements GetCrewHubUseCase {

	private static final String HUB_ACCESS_DENIED_MESSAGE = "크루 허브는 프로필 보완 완료 사용자만 접근할 수 있습니다.";
	private static final String MEMBER_ONLY_MESSAGE = "가입한 크루원만 크루 허브를 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewHubView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), HUB_ACCESS_DENIED_MESSAGE);

		CrewHubView view = crewQueryRepository.findCrewHubViewByCrewIdAndUserId(query.crewId(), query.userId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (view.myRole() == null) {
			throw new AccessDeniedException(MEMBER_ONLY_MESSAGE);
		}
		return view;
	}
}
