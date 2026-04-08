package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewAlreadyJoinedException;
import com.bangpot.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewJoinRequestNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinViewStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestCrewJoinService implements RequestCrewJoinUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));
		AuthUser requester = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));

		if (requester.requiresCompletion()) {
			throw new AccessDeniedException("크루 가입 신청 권한이 없습니다.");
		}
		if (crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), requester.getId())) {
			throw new CrewAlreadyJoinedException(crew.getId(), requester.getId());
		}
		if (crewJoinRequestRepository.existsPendingByCrewIdAndUserId(crew.getId(), requester.getId())) {
			throw new CrewJoinRequestAlreadyPendingException(crew.getId(), requester.getId());
		}
		if (!crew.allowsDirectJoinRequest()) {
			throw new CrewJoinRequestNotAllowedException(crew.getId());
		}

		crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), normalizeOptional(command.message()))
		);
		return Result.of(crew.getId(), CrewJoinViewStatus.PENDING);
	}

	private String normalizeOptional(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
