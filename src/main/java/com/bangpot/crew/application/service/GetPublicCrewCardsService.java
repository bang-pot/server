package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicCrewCardsService implements GetPublicCrewCardsUseCase {

	private final CrewRepository crewRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle() {
		return crewRepository.findPublicCrews().stream()
			.map(crew -> View.of(
				crew.getId(),
				crew.getName(),
				crew.getDescription(),
				crew.getVisibility().name(),
				crew.getImageUrl()
			))
			.toList();
	}
}
