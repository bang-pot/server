package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelMeetingService implements CancelMeetingUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("모임 개설자 또는 크루장만 모임을 취소할 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("모임 개설자 또는 크루장만 모임을 취소할 수 있습니다.");
		}

		Meeting meeting = meetingRepository.findByIdAndCrewId(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		boolean isHost = meeting.getHostUserId().equals(command.userId());
		boolean isLeader = crewMemberRepository.existsLeaderByCrewIdAndUserId(command.crewId(), command.userId());
		if (!isHost && !isLeader) {
			throw new AccessDeniedException("모임 개설자 또는 크루장만 모임을 취소할 수 있습니다.");
		}

		meeting.cancel();
		meetingRepository.save(meeting);
		return Result.of(meeting.getId(), meeting.getStatus().name());
	}
}
