package com.banglog.crew.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewLeaderLeaveNotAllowedException;
import com.banglog.crew.application.exception.CrewLeaveNotAllowedForHostedMeetingException;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.usecase.LeaveCrewUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.CleanupMeetingsForInactiveCrewMemberUseCase;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveCrewService implements LeaveCrewUseCase {

	private static final String LEAVE_DENIED_MESSAGE = "가입한 크루원만 크루를 탈퇴할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final CleanupMeetingsForInactiveCrewMemberUseCase cleanupMeetingsForInactiveCrewMemberUseCase;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), LEAVE_DENIED_MESSAGE);

		Crew crew = crewRepository.findByIdForUpdate(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		CrewMember crewMember = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.userId())
			.orElseThrow(() -> new AccessDeniedException(LEAVE_DENIED_MESSAGE));

		if (crewMember.getRole() == CrewRole.LEADER) {
			throw new CrewLeaderLeaveNotAllowedException(crew.getId(), command.userId());
		}

		boolean hasHostedUnfinishedMeeting = meetingRepository.existsUnfinishedByCrewIdAndHostUserId(
			crew.getId(),
			command.userId()
		);
		if (hasHostedUnfinishedMeeting) {
			throw new CrewLeaveNotAllowedForHostedMeetingException(crew.getId(), command.userId());
		}

		cleanupMeetingsForInactiveCrewMemberUseCase.handle(
			CleanupMeetingsForInactiveCrewMemberUseCase.InactiveCrewMember.of(
				crew.getId(),
				command.userId()
			)
		);

		crewMember.leave();
		crewMemberRepository.save(crewMember);
		return Result.of(crew.getId());
	}
}
