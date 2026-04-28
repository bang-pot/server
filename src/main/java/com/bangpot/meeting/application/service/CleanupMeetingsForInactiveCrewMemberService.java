package com.bangpot.meeting.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.CleanupMeetingsForInactiveCrewMemberUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CleanupMeetingsForInactiveCrewMemberService implements CleanupMeetingsForInactiveCrewMemberUseCase {

	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final Clock clock;

	@Override
	@Transactional
	public void handle(InactiveCrewMember inactiveCrewMember) {
		Instant cleanedAt = clock.instant();
		meetingRepository.cancelUnfinishedByCrewIdAndHostUserId(
			inactiveCrewMember.crewId(),
			inactiveCrewMember.userId(),
			cleanedAt
		);
		meetingParticipantRepository.leaveInactiveCrewMemberParticipations(
			inactiveCrewMember.crewId(),
			inactiveCrewMember.userId(),
			cleanedAt
		);
	}
}
