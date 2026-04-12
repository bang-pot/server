package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.meeting.domain.MeetingParticipationRequest;

interface MeetingParticipationRequestJpaRepository extends JpaRepository<MeetingParticipationRequest, Long> {

	Optional<MeetingParticipationRequest> findByMeetingIdAndUserId(Long meetingId, Long userId);
}
