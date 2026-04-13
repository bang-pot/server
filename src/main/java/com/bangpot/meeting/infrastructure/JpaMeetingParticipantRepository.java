package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.domain.MeetingParticipant;

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
	public void delete(MeetingParticipant participant) {
		meetingParticipantJpaRepository.delete(participant);
	}
}
