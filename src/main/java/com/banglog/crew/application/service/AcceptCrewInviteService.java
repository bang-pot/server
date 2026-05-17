package com.banglog.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewInviteNotFoundException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewInviteRepository;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.AcceptCrewInviteUseCase;
import com.banglog.crew.domain.CrewInvite;
import com.banglog.crew.domain.CrewInviteStatus;
import com.banglog.crew.domain.CrewMember;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AcceptCrewInviteService implements AcceptCrewInviteUseCase {

	private static final String ACCEPT_DENIED_MESSAGE = "초대를 수락할 권한이 없습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), ACCEPT_DENIED_MESSAGE);

		CrewInvite invite = crewInviteRepository.findPendingByIdAndTargetUserIdForUpdate(command.inviteId(), command.userId())
			.orElseThrow(() -> new CrewInviteNotFoundException(command.inviteId()));

		crewRepository.findByIdForUpdate(invite.getCrewId())
			.orElseThrow(() -> new CrewNotFoundException(invite.getCrewId()));

		Optional<CrewMember> existingMembership = crewMemberRepository.findAnyByCrewIdAndUserId(
				invite.getCrewId(),
				command.userId()
		);

		if (existingMembership.isPresent()) {
			CrewMember crewMember = existingMembership.get();
			if (!crewMember.isActive()) {
				crewMember.reactivateAsMember();
				crewMemberRepository.save(crewMember);
			}
		} else {
			crewMemberRepository.save(CrewMember.createMember(invite.getCrewId(), command.userId()));
		}

		invite.approve();
		crewInviteRepository.save(invite);

		return Result.of(invite.getId(), invite.getCrewId(), CrewInviteStatus.APPROVED.name());
	}
}
