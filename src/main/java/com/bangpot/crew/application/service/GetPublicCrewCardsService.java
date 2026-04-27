package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.domain.view.PublicCrewCardsView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicCrewCardsService implements GetPublicCrewCardsUseCase {

	private final CrewQueryRepository crewQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public PublicCrewCardsView handle(Query query) {
		return crewQueryRepository.findPublicCrewCardsView(query.page(), query.size());
	}
}
