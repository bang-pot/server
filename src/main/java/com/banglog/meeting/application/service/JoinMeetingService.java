package com.banglog.meeting.application.service;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.meeting.application.exception.MeetingNotFoundException;
import com.banglog.meeting.application.exception.MeetingParticipationAlreadyJoinedException;
import com.banglog.meeting.application.port.MeetingParticipantRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.JoinMeetingUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.user.application.service.CompletedUserAccessService;

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
