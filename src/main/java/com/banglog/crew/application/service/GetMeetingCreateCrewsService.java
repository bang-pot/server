package com.banglog.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.application.usecase.GetMeetingCreateCrewsUseCase;
import com.banglog.crew.domain.view.MeetingCreateCrewsView;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMeetingCreateCrewsService implements GetMeetingCreateCrewsUseCase {
	private static final String ACCESS_DENIED_MESSAGE = "완료된 사용자만 모임 생성 크루 목록을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	public MeetingCreateCrewsView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		return crewQueryRepository.findActiveCrewsByUserId(query.userId());
	}
}
