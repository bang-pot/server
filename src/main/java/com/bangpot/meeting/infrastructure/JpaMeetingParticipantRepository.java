package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingParticipantRepository implements MeetingParticipantRepository {

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
	public long countByMeetingId(Long meetingId) {
		return meetingParticipantJpaRepository.countByMeetingIdAndStatusIn(
			meetingId,
			List.of(
				MeetingParticipationStatus.JOINED,
				MeetingParticipationStatus.PENDING,
				MeetingParticipationStatus.APPROVED
			)
		);
	}

	@Override
	public void delete(MeetingParticipant participant) {
		meetingParticipantJpaRepository.delete(participant);
	}
}
