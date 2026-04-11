package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewInviteNotFoundException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.crew.domain.CrewMember;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcceptCrewInviteService implements AcceptCrewInviteUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		AuthUser user = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (user.requiresCompletion()) {
			throw new AccessDeniedException("초대를 수락할 권한이 없습니다.");
		}

		CrewInvite invite = crewInviteRepository.findPendingByIdAndTargetUserId(command.inviteId(), command.userId())
			.orElseThrow(() -> new CrewInviteNotFoundException(command.inviteId()));

		crewRepository.findById(invite.getCrewId())
			.orElseThrow(() -> new CrewNotFoundException(invite.getCrewId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(invite.getCrewId(), command.userId())) {
			crewMemberRepository.save(CrewMember.createMember(invite.getCrewId(), command.userId()));
		}
		invite.approve();
		crewInviteRepository.save(invite);

		return Result.of(invite.getId(), invite.getCrewId(), CrewInviteStatus.APPROVED.name());
	}
}
