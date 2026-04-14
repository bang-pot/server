package com.bangpot.explore.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.explore.application.usecase.GetExploreMeetingCreateCrewsUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExploreMeetingCreateCrewsService implements GetExploreMeetingCreateCrewsUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "모임 생성용 크루 목록을 조회할 수 없습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		List<CrewItem> crews = crewMemberRepository.findAllByUserId(query.userId()).stream()
			.map(member -> crewRepository.findById(member.getCrewId())
				.map(crew -> CrewItem.of(crew.getId(), crew.getName()))
				.orElse(null))
			.filter(item -> item != null)
			.toList();

		return Result.of(crews);
	}
}
