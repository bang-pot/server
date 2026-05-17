package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewAlreadyJoinedException;
import com.banglog.crew.application.exception.CrewInviteAlreadyPendingException;
import com.banglog.crew.application.exception.CrewInviteNotAllowedException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.exception.CrewSelfInviteNotAllowedException;
import com.banglog.crew.application.port.CrewInviteRepository;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.CreateCrewInviteUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewInvite;
import com.banglog.crew.domain.CrewInviteStatus;
import com.banglog.user.application.exception.UserNotFoundException;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.application.service.CompletedUserAccessService;
import com.banglog.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCrewInviteService implements CreateCrewInviteUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findByIdForUpdate(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));
		completedUserAccessService.validateCompletedUser(command.inviterUserId(), "크루 초대를 보낼 권한이 없습니다.");
		User target = userRepository.findById(command.targetUserId())
			.orElseThrow(() -> new UserNotFoundException(command.targetUserId()));

		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(crew.getId(), command.inviterUserId())) {
			throw new AccessDeniedException("크루 초대를 보낼 권한이 없습니다.");
		}
		if (command.inviterUserId().equals(command.targetUserId())) {
			throw new CrewSelfInviteNotAllowedException(crew.getId(), command.inviterUserId());
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

		crewInviteRepository.save(CrewInvite.createPending(crew.getId(), command.inviterUserId(), target.getId()));
		return Result.of(crew.getId(), target.getId(), CrewInviteStatus.PENDING.name());
	}
}
