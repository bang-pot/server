package com.banglog.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.meeting.application.exception.MeetingNotFoundException;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloseMeetingRecruitmentService implements CloseMeetingRecruitmentUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), "가입한 크루원만 모집마감을 변경할 수 있습니다.");
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 모집마감을 변경할 수 있습니다.");
		}

		Meeting meeting = meetingRepository.findByIdAndCrewIdForUpdate(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		if (!meeting.getHostUserId().equals(command.userId())) {
			throw new AccessDeniedException("모임 개설자만 모집마감을 변경할 수 있습니다.");
		}

		meeting.closeRecruitment();
		return Result.of(meeting.getId(), meeting.getStatus().name());
	}
}
