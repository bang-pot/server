package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelMeetingService implements CancelMeetingUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), "가입한 크루원만 모임 취소를 실행할 수 있습니다.");
		CrewMember crewMember = crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId())
			.orElseThrow(() -> new AccessDeniedException("가입한 크루원만 모임 취소를 실행할 수 있습니다."));

		Meeting meeting = meetingRepository.findByIdAndCrewIdForUpdate(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));

		boolean isHost = meeting.getHostUserId().equals(command.userId());
		boolean isLeader = crewMember.getRole() == CrewRole.LEADER;
		if (!isHost && !isLeader) {
			throw new AccessDeniedException("모임 개설자 또는 크루장만 모임 취소를 실행할 수 있습니다.");
		}

		meeting.cancel();
		return Result.of(meeting.getId(), meeting.getStatus().name());
	}
}
