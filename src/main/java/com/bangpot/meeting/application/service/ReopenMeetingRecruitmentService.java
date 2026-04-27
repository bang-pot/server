package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReopenMeetingRecruitmentService implements ReopenMeetingRecruitmentUseCase {

	private static final String MEMBER_ONLY_MESSAGE = "가입한 크루원만 모집을 재개할 수 있습니다.";
	private static final String HOST_ONLY_MESSAGE = "모임 개설자만 모집을 재개할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingAutomaticTransitionService meetingAutomaticTransitionService;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findByIdForShare(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), MEMBER_ONLY_MESSAGE);
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException(MEMBER_ONLY_MESSAGE);
		}

		Meeting meeting = meetingRepository.findByIdAndCrewId(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		meetingAutomaticTransitionService.apply(meeting);
		if (!meeting.getHostUserId().equals(command.userId())) {
			throw new AccessDeniedException(HOST_ONLY_MESSAGE);
		}

		meeting.reopenRecruitment();
		meetingRepository.save(meeting);
		return Result.of(meeting.getId(), meeting.getStatus().name());
	}
}
