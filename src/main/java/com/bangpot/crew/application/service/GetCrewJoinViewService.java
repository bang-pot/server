package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinViewStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewJoinViewService implements GetCrewJoinViewUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		Crew crew = crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		CrewJoinViewStatus myStatus = resolveMyStatus(query.userId(), crew);
		return Result.of(
			crew.getId(),
			crew.getName(),
			crew.getDescription(),
			crew.getVisibility().name(),
			crew.getImageUrl(),
			myStatus
		);
	}

	private CrewJoinViewStatus resolveMyStatus(Long userId, Crew crew) {
		if (userId == null) {
			return CrewJoinViewStatus.GUEST;
		}

		AuthUser authUser = authUserRepository.findById(userId)
			.orElseThrow(() -> new AuthUserNotFoundException(userId));

		if (authUser.requiresCompletion()) {
			return CrewJoinViewStatus.COMPLETION_REQUIRED;
		}

		if (crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), userId)) {
			return CrewJoinViewStatus.MEMBER;
		}

		if (crewJoinRequestRepository.existsPendingByCrewIdAndUserId(crew.getId(), userId)) {
			return CrewJoinViewStatus.PENDING;
		}

		if (crew.allowsDirectJoinRequest()) {
			return CrewJoinViewStatus.CAN_REQUEST;
		}

		return CrewJoinViewStatus.PRIVATE_RESTRICTED;
	}
}
