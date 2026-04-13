package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	long countByMeetingIdAndStatusIn(Long meetingId, List<MeetingParticipationStatus> statuses);
}
