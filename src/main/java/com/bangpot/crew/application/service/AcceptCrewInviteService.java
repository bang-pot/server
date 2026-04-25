package com.bangpot.crew.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewInviteNotFoundException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

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

		CrewInvite invite = crewInviteRepository.findPendingByIdAndTargetUserId(command.inviteId(), command.userId())
			.orElseThrow(() -> new CrewInviteNotFoundException(command.inviteId()));

		crewRepository.findByIdForShare(invite.getCrewId())
			.orElseThrow(() -> new CrewNotFoundException(invite.getCrewId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(invite.getCrewId(), command.userId())) {
			var existingMembership = crewMemberRepository.findAnyByCrewIdAndUserId(invite.getCrewId(), command.userId());
			if (existingMembership.isPresent()) {
				CrewMember crewMember = existingMembership.get();
				crewMember.reactivateAsMember();
				crewMemberRepository.save(crewMember);
			} else {
				crewMemberRepository.save(CrewMember.createMember(invite.getCrewId(), command.userId()));
			}
		}
		invite.approve();
		crewInviteRepository.save(invite);

		return Result.of(invite.getId(), invite.getCrewId(), CrewInviteStatus.APPROVED.name());
	}
}
