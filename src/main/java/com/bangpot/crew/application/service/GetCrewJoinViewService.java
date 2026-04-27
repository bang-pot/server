package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.domain.view.CrewJoinView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewJoinViewService implements GetCrewJoinViewUseCase {

	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewJoinView handle(Query query) {
		return crewQueryRepository.findCrewJoinViewByCrewIdAndUserId(query.crewId(), query.userId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));
	}
}
