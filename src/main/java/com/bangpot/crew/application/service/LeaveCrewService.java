package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewLeaderLeaveNotAllowedException;
import com.bangpot.crew.application.exception.CrewLeaveNotAllowedForHostedMeetingException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.LeaveCrewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveCrewService implements LeaveCrewUseCase {

	private static final String LEAVE_DENIED_MESSAGE = "가입한 크루원만 크루를 탈퇴할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), LEAVE_DENIED_MESSAGE);

		CrewMember crewMember = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.userId())
			.orElseThrow(() -> new AccessDeniedException(LEAVE_DENIED_MESSAGE));

		if (crewMember.getRole() == CrewRole.LEADER) {
			throw new CrewLeaderLeaveNotAllowedException(crew.getId(), command.userId());
		}

		boolean hasHostedUnfinishedMeeting = meetingRepository.existsByCrewIdAndHostUserIdAndStatusIn(
			crew.getId(),
			command.userId(),
			List.of(MeetingStatus.RECRUITING, MeetingStatus.RECRUITMENT_CLOSED)
		);
		if (hasHostedUnfinishedMeeting) {
			throw new CrewLeaveNotAllowedForHostedMeetingException(crew.getId(), command.userId());
		}

		crewMemberRepository.deleteByCrewIdAndUserId(crew.getId(), command.userId());
		return Result.of(crew.getId());
	}
}
