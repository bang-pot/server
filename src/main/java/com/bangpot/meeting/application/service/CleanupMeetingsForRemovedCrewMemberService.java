package com.bangpot.meeting.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.CleanupMeetingsForRemovedCrewMemberUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CleanupMeetingsForRemovedCrewMemberService implements CleanupMeetingsForRemovedCrewMemberUseCase {

	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	@Override
	@Transactional
	public void handle(RemovedCrewMember removedCrewMember) {
		Instant cleanedAt = Instant.now();
		meetingRepository.cancelUnfinishedByCrewIdAndHostUserId(
			removedCrewMember.crewId(),
			removedCrewMember.userId(),
			cleanedAt
		);
		meetingParticipantRepository.leaveJoinedByCrewIdAndUserIdInUnfinishedMeetings(
			removedCrewMember.crewId(),
			removedCrewMember.userId(),
			cleanedAt
		);
	}
}
