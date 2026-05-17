package com.banglog.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.meeting.application.exception.MeetingHostCannotCancelParticipationException;
import com.banglog.meeting.application.exception.MeetingNotFoundException;
import com.banglog.meeting.application.exception.MeetingParticipationNotJoinedException;
import com.banglog.meeting.application.port.MeetingParticipantRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelMeetingParticipationService implements CancelMeetingParticipationUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), "가입한 크루원만 참여취소할 수 있습니다.");
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 참여취소할 수 있습니다.");
		}

		Meeting meeting = meetingRepository.findByIdAndCrewIdForUpdate(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		if (meeting.getHostUserId().equals(command.userId())) {
			throw new MeetingHostCannotCancelParticipationException(meeting.getId(), command.userId());
		}

		MeetingParticipant participant = meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), command.userId())
			.orElseThrow(() -> new MeetingParticipationNotJoinedException(meeting.getId(), command.userId()));
		if (!participant.getStatus().representsJoined()) {
			throw new MeetingParticipationNotJoinedException(meeting.getId(), command.userId());
		}

		participant.leave();
		return Result.of(meeting.getId(), MeetingParticipationStatus.NOT_JOINED.name());
	}
}
