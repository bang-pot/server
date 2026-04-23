package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.usecase.CancelCrewJoinRequestUseCase;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.user.application.port.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CancelCrewJoinRequestService implements CancelCrewJoinRequestUseCase {

	private final UserRepository userRepository;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	public Result handle(Command command) {
		if (userRepository.findById(command.userId()).isEmpty()) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		CrewJoinRequest joinRequest = crewJoinRequestRepository.findPendingByIdAndUserId(
			command.requestId(),
			command.userId()
		).orElseThrow(() -> new CrewJoinRequestNotFoundException(command.requestId()));

		joinRequest.cancel();
		crewJoinRequestRepository.save(joinRequest);
		return Result.of(joinRequest.getId(), joinRequest.getCrewId());
	}
}
