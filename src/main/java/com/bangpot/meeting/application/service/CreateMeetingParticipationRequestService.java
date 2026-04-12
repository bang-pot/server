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
import com.bangpot.meeting.application.exception.MeetingParticipationAlreadyApprovedException;
import com.bangpot.meeting.application.exception.MeetingParticipationAlreadyPendingException;
import com.bangpot.meeting.application.port.MeetingParticipationRequestRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.CreateMeetingParticipationRequestUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationRequest;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMeetingParticipationRequestService implements CreateMeetingParticipationRequestUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipationRequestRepository meetingParticipationRequestRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("가입한 크루원만 모임 참가 신청을 보낼 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 모임 참가 신청을 보낼 수 있습니다.");
		}

		Meeting meeting = meetingRepository.findByIdAndCrewId(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));

		if (meeting.getHostUserId().equals(command.userId())) {
			throw new MeetingParticipationAlreadyApprovedException(meeting.getId(), command.userId());
		}

		meetingParticipationRequestRepository.findByMeetingIdAndUserId(meeting.getId(), command.userId())
			.ifPresent(request -> {
				if (request.getStatus() == MeetingParticipationStatus.PENDING) {
					throw new MeetingParticipationAlreadyPendingException(meeting.getId(), command.userId());
				}
				if (request.getStatus() == MeetingParticipationStatus.APPROVED) {
					throw new MeetingParticipationAlreadyApprovedException(meeting.getId(), command.userId());
				}
			});

		meetingParticipationRequestRepository.save(MeetingParticipationRequest.createPending(meeting.getId(), command.userId()));
		return Result.of(meeting.getId(), MeetingParticipationStatus.PENDING.name());
	}
}
