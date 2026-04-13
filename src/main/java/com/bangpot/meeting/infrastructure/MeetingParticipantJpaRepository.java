package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.meeting.domain.MeetingParticipant;

interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	long countByMeetingId(Long meetingId);
}
