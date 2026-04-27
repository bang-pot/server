package com.bangpot.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.exception.CrewRemoveMemberTargetNotAllowedException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.RemoveCrewMemberUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.usecase.CleanupMeetingsForRemovedCrewMemberUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemoveCrewMemberService implements RemoveCrewMemberUseCase {

	private static final String REMOVE_DENIED_MESSAGE = "현재 크루장만 크루원을 강제 제거할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final CleanupMeetingsForRemovedCrewMemberUseCase cleanupMeetingsForRemovedCrewMemberUseCase;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.leaderUserId(), REMOVE_DENIED_MESSAGE);

		Crew crew = crewRepository.findByIdForUpdate(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		CrewMember currentLeader = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.leaderUserId())
			.orElseThrow(() -> new AccessDeniedException(REMOVE_DENIED_MESSAGE));
		if (currentLeader.getRole() != CrewRole.LEADER) {
			throw new AccessDeniedException(REMOVE_DENIED_MESSAGE);
		}

		CrewMember targetMember = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.targetUserId())
			.orElseThrow(() -> new CrewRemoveMemberTargetNotAllowedException(crew.getId(), command.targetUserId()));
		if (targetMember.getRole() != CrewRole.MEMBER) {
			throw new CrewRemoveMemberTargetNotAllowedException(crew.getId(), command.targetUserId());
		}

		cleanupMeetingsForRemovedCrewMemberUseCase.handle(
			CleanupMeetingsForRemovedCrewMemberUseCase.RemovedCrewMember.of(
				crew.getId(),
				command.targetUserId()
			)
		);

		targetMember.remove();
		crewMemberRepository.save(targetMember);
		return Result.of(crew.getId(), command.targetUserId());
	}
}
