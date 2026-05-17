package com.banglog.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewJoinRequestNotFoundException;
import com.banglog.crew.application.port.CrewJoinRequestRepository;
import com.banglog.crew.application.usecase.CancelCrewJoinRequestUseCase;
import com.banglog.crew.domain.CrewJoinRequest;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CancelCrewJoinRequestService implements CancelCrewJoinRequestUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewJoinRequestRepository crewJoinRequestRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "가입 신청 취소 권한이 없습니다.");

		CrewJoinRequest joinRequest = crewJoinRequestRepository.findPendingByIdAndUserIdForUpdate(
			command.requestId(),
			command.userId()
		).orElseThrow(() -> new CrewJoinRequestNotFoundException(command.requestId()));

		joinRequest.cancel();
		crewJoinRequestRepository.save(joinRequest);
		return Result.of(joinRequest.getId(), joinRequest.getCrewId());
	}
}
