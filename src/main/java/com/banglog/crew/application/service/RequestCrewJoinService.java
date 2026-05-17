package com.banglog.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewAlreadyJoinedException;
import com.banglog.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.banglog.crew.application.exception.CrewJoinRequestNotAllowedException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewJoinRequestRepository;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.RequestCrewJoinUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewJoinRequest;
import com.banglog.crew.domain.CrewJoinViewStatus;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestCrewJoinService implements RequestCrewJoinUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "크루 가입 신청 권한이 없습니다.");

		Crew crew = crewRepository.findByIdForUpdate(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		if (crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), command.userId())) {
			throw new CrewAlreadyJoinedException(crew.getId(), command.userId());
		}
		if (crewJoinRequestRepository.existsPendingByCrewIdAndUserId(crew.getId(), command.userId())) {
			throw new CrewJoinRequestAlreadyPendingException(crew.getId(), command.userId());
		}
		if (!crew.allowsDirectJoinRequest()) {
			throw new CrewJoinRequestNotAllowedException(crew.getId());
		}

		crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), command.userId(), normalizeOptional(command.message()))
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
