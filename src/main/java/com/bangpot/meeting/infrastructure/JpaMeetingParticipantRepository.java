package com.bangpot.meeting.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingParticipantRepository implements MeetingParticipantRepository {

	private static final List<MeetingStatus> UNFINISHED_MEETING_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED
	);

	private static final List<MeetingParticipationStatus> JOINED_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	private final MeetingParticipantJpaRepository meetingParticipantJpaRepository;

	@Override
	public MeetingParticipant save(MeetingParticipant participant) {
		return meetingParticipantJpaRepository.save(participant);
	}

	@Override
	public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantJpaRepository.findByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public int leaveJoinedByCrewIdAndUserIdInUnfinishedMeetings(Long crewId, Long userId, Instant updatedAt) {
		return meetingParticipantJpaRepository.leaveJoinedByCrewIdAndUserIdInUnfinishedMeetings(
			crewId,
			userId,
			UNFINISHED_MEETING_STATUSES,
			JOINED_STATUSES,
			MeetingParticipationStatus.LEFT,
			updatedAt
		);
	}

	@Override
	public long countByMeetingId(Long meetingId) {
		return meetingParticipantJpaRepository.countByMeetingIdAndStatusIn(meetingId, JOINED_STATUSES);
	}

	@Override
	public void delete(MeetingParticipant participant) {
		meetingParticipantJpaRepository.delete(participant);
	}
}
