package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingParticipationRequestRepository;
import com.bangpot.meeting.domain.MeetingParticipationRequest;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingParticipationRequestRepository implements MeetingParticipationRequestRepository {

	private final MeetingParticipationRequestJpaRepository meetingParticipationRequestJpaRepository;

	@Override
	public MeetingParticipationRequest save(MeetingParticipationRequest request) {
		return meetingParticipationRequestJpaRepository.save(request);
	}

	@Override
	public Optional<MeetingParticipationRequest> findByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipationRequestJpaRepository.findByMeetingIdAndUserId(meetingId, userId);
	}
}
