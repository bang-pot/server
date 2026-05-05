package com.bangpot.meeting.application.service;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JoinMeetingService implements JoinMeetingUseCase {

	private static final String CREW_MEMBER_JOIN_REQUIRED_MESSAGE = "가입한 크루원만 모임에 참여할 수 있습니다.";
	private static final String RECRUITING_MEETING_REQUIRED_MESSAGE = "모집 중인 모임만 참여할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final MeetingRecruitmentCloseService meetingRecruitmentCloseService;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), CREW_MEMBER_JOIN_REQUIRED_MESSAGE);
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException(CREW_MEMBER_JOIN_REQUIRED_MESSAGE);
		}

		Meeting meeting = meetingRepository.findByIdAndCrewIdForUpdate(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		meetingRecruitmentCloseService.closeAndSaveIfNeeded(meeting);
		if (meeting.getStatus() != MeetingStatus.RECRUITING) {
			throw new AccessDeniedException(RECRUITING_MEETING_REQUIRED_MESSAGE);
		}

		if (meeting.getHostUserId().equals(command.userId())) {
			throw new MeetingParticipationAlreadyJoinedException(meeting.getId(), command.userId());
		}

		Optional<MeetingParticipant> existingParticipant = meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), command.userId());
		if (existingParticipant.isPresent()) {
			MeetingParticipant participant = existingParticipant.get();
			if (participant.getStatus().representsJoined()) {
				throw new MeetingParticipationAlreadyJoinedException(meeting.getId(), command.userId());
			}
			participant.rejoin();
		} else {
			meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), command.userId()));
		}
		meetingRecruitmentCloseService.closeAndSaveIfNeeded(meeting);
		return Result.of(meeting.getId(), MeetingParticipationStatus.JOINED.name());
	}
}
