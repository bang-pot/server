package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.exception.CrewTransferLeadershipTargetNotAllowedException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferCrewLeadershipService implements TransferCrewLeadershipUseCase {

	private static final String TRANSFER_DENIED_MESSAGE = "현재 크루장만 크루장을 위임할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.leaderUserId(), TRANSFER_DENIED_MESSAGE);

		CrewMember currentLeader = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.leaderUserId())
			.orElseThrow(() -> new AccessDeniedException(TRANSFER_DENIED_MESSAGE));
		if (currentLeader.getRole() != CrewRole.LEADER) {
			throw new AccessDeniedException(TRANSFER_DENIED_MESSAGE);
		}

		CrewMember targetMember = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.targetUserId())
			.orElseThrow(() -> new CrewTransferLeadershipTargetNotAllowedException(crew.getId(), command.targetUserId()));
		if (targetMember.getRole() != CrewRole.MEMBER) {
			throw new CrewTransferLeadershipTargetNotAllowedException(crew.getId(), command.targetUserId());
		}

		currentLeader.transferLeadershipToMember();
		targetMember.transferLeadershipToLeader();
		crewMemberRepository.save(currentLeader);
		crewMemberRepository.save(targetMember);

		return Result.of(crew.getId(), targetMember.getUserId());
	}
}
