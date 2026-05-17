package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewDeleteNameMismatchException;
import com.banglog.crew.application.exception.CrewDeleteNotAllowedWithActiveMeetingsException;
import com.banglog.crew.application.exception.CrewDeleteNotAllowedWithActiveMembersException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.DeleteCrewUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCrewService implements DeleteCrewUseCase {

	private static final String DELETE_DENIED_MESSAGE = "현재 크루장만 크루를 삭제할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.leaderUserId(), DELETE_DENIED_MESSAGE);

		Crew crew = crewRepository.findByIdForUpdate(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		CrewMember currentLeader = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.leaderUserId())
			.orElseThrow(() -> new AccessDeniedException(DELETE_DENIED_MESSAGE));
		if (currentLeader.getRole() != CrewRole.LEADER) {
			throw new AccessDeniedException(DELETE_DENIED_MESSAGE);
		}

		if (!crew.getName().equals(command.crewName())) {
			throw new CrewDeleteNameMismatchException();
		}

		boolean hasOtherActiveMembers = crewMemberRepository.existsActiveByCrewIdAndUserIdNot(
			crew.getId(),
			command.leaderUserId()
		);
		if (hasOtherActiveMembers) {
			throw new CrewDeleteNotAllowedWithActiveMembersException();
		}

		boolean hasUnfinishedMeetings = meetingRepository.existsUnfinishedByCrewId(crew.getId());
		if (hasUnfinishedMeetings) {
			throw new CrewDeleteNotAllowedWithActiveMeetingsException();
		}

		currentLeader.leave();
		crew.delete();
		crewMemberRepository.save(currentLeader);
		crewRepository.save(crew);
		return Result.of(crew.getId());
	}
}
