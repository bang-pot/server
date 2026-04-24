package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.usecase.GetMeetingCreateCrewsUseCase;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMeetingCreateCrewsService implements GetMeetingCreateCrewsUseCase {
	private static final String ACCESS_DENIED_MESSAGE = "모임 생성용 크루 목록을 조회할 권한이 없습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		List<CrewItem> crews = crewQueryRepository.findMeetingCreateCrewsByMemberUserId(query.userId()).items().stream()
			.map(this::toCrewItem)
			.toList();

		return Result.of(crews);
	}

	private CrewItem toCrewItem(MeetingCreateCrewsView.Item item) {
		return CrewItem.of(item.crewId(), item.crewName());
	}
}
