package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewInviteNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RejectCrewInviteService implements RejectCrewInviteUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "초대를 거절할 권한이 없습니다.");

		CrewInvite invite = crewInviteRepository.findPendingByIdAndTargetUserId(command.inviteId(), command.userId())
			.orElseThrow(() -> new CrewInviteNotFoundException(command.inviteId()));

		invite.reject();
		crewInviteRepository.save(invite);
		return Result.of(invite.getId(), invite.getCrewId(), CrewInviteStatus.REJECTED.name());
	}
}
