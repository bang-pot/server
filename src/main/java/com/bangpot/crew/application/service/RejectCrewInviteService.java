package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewInviteNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RejectCrewInviteService implements RejectCrewInviteUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		AuthUser user = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (user.requiresCompletion()) {
			throw new AccessDeniedException("초대를 거절할 권한이 없습니다.");
		}

		CrewInvite invite = crewInviteRepository.findPendingByIdAndTargetUserId(command.inviteId(), command.userId())
			.orElseThrow(() -> new CrewInviteNotFoundException(command.inviteId()));

		invite.reject();
		crewInviteRepository.save(invite);
		return Result.of(invite.getId(), invite.getCrewId(), CrewInviteStatus.REJECTED.name());
	}
}
