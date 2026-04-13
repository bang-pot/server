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
import com.bangpot.meeting.application.exception.MeetingParticipationAlreadyJoinedException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JoinMeetingService implements JoinMeetingUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final MeetingAutomaticTransitionService meetingAutomaticTransitionService;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("가입한 크루원만 모임에 참여할 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 모임에 참여할 수 있습니다.");
		}

		Meeting meeting = meetingRepository.findByIdAndCrewId(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		meetingAutomaticTransitionService.apply(meeting);
		if (meeting.getStatus() != com.bangpot.meeting.domain.MeetingStatus.RECRUITING) {
			throw new AccessDeniedException("모집 중인 모임만 참여할 수 있습니다.");
		}

		if (meeting.getHostUserId().equals(command.userId())) {
			throw new MeetingParticipationAlreadyJoinedException(meeting.getId(), command.userId());
		}

		if (meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), command.userId()).isPresent()) {
			throw new MeetingParticipationAlreadyJoinedException(meeting.getId(), command.userId());
		}

		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), command.userId()));
		meetingAutomaticTransitionService.apply(meeting);
		return Result.of(meeting.getId(), MeetingParticipationStatus.JOINED.name());
	}
}
