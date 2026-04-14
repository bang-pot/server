package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewDeleteNameMismatchException;
import com.bangpot.crew.application.exception.CrewDeleteNotAllowedWithActiveMeetingsException;
import com.bangpot.crew.application.exception.CrewDeleteNotAllowedWithActiveMembersException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.DeleteCrewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

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
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.leaderUserId(), DELETE_DENIED_MESSAGE);

		CrewMember currentLeader = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.leaderUserId())
			.orElseThrow(() -> new AccessDeniedException(DELETE_DENIED_MESSAGE));
		if (currentLeader.getRole() != CrewRole.LEADER) {
			throw new AccessDeniedException(DELETE_DENIED_MESSAGE);
		}

		boolean hasOtherActiveMembers = crewMemberRepository.findAllByCrewId(crew.getId()).stream()
			.anyMatch(member -> !member.getUserId().equals(command.leaderUserId()));
		if (hasOtherActiveMembers) {
			throw new CrewDeleteNotAllowedWithActiveMembersException();
		}

		boolean hasUnfinishedMeetings = meetingRepository.existsByCrewIdAndStatusIn(
			crew.getId(),
			List.of(MeetingStatus.RECRUITING, MeetingStatus.RECRUITMENT_CLOSED)
		);
		if (hasUnfinishedMeetings) {
			throw new CrewDeleteNotAllowedWithActiveMeetingsException();
		}

		if (!crew.getName().equals(command.crewName())) {
			throw new CrewDeleteNameMismatchException();
		}

		currentLeader.leave();
		crew.delete();
		crewMemberRepository.save(currentLeader);
		crewRepository.save(crew);
		return Result.of(crew.getId());
	}
}
