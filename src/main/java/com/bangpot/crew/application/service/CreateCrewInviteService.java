package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewAlreadyJoinedException;
import com.bangpot.crew.application.exception.CrewInviteAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewInviteNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.CreateCrewInviteUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCrewInviteService implements CreateCrewInviteUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));
		AuthUser inviter = authUserRepository.findById(command.inviterUserId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.inviterUserId()));
		User target = userRepository.findById(command.targetUserId())
			.orElseThrow(() -> new UserNotFoundException(command.targetUserId()));

		if (inviter.requiresCompletion()) {
			throw new AccessDeniedException("크루 초대를 보낼 권한이 없습니다.");
		}
		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crew.getId(), inviter.getId())) {
			throw new AccessDeniedException("크루 초대를 보낼 권한이 없습니다.");
		}
		if (!crew.allowsDirectInvite()) {
			throw new CrewInviteNotAllowedException(crew.getId());
		}
		if (crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), target.getId())) {
			throw new CrewAlreadyJoinedException(crew.getId(), target.getId());
		}
		if (crewInviteRepository.existsPendingByCrewIdAndTargetUserId(crew.getId(), target.getId())) {
			throw new CrewInviteAlreadyPendingException(crew.getId(), target.getId());
		}

		crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId()));
		return Result.of(crew.getId(), target.getId(), CrewInviteStatus.PENDING.name());
	}
}
