package com.banglog.meeting.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.meeting.application.exception.MeetingNotFoundException;
import com.banglog.meeting.application.port.MeetingParticipantRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReopenMeetingRecruitmentService implements ReopenMeetingRecruitmentUseCase {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");
	private static final String MEMBER_ONLY_MESSAGE = "가입한 크루원만 모집을 재개할 수 있습니다.";
	private static final String HOST_ONLY_MESSAGE = "모임 개설자만 모집을 재개할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findByIdForShare(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), MEMBER_ONLY_MESSAGE);
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException(MEMBER_ONLY_MESSAGE);
		}

		Meeting meeting = meetingRepository.findByIdAndCrewIdForUpdate(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		if (!meeting.getHostUserId().equals(command.userId())) {
			throw new AccessDeniedException(HOST_ONLY_MESSAGE);
		}

		long joinedCount = meetingParticipantRepository.countByMeetingId(meeting.getId()) + 1L;
		meeting.reopenRecruitment(now(), joinedCount);
		return Result.of(meeting.getId(), meeting.getStatus().name());
	}

	private LocalDateTime now() {
		return clock.instant().atZone(BUSINESS_ZONE).toLocalDateTime();
	}
}
