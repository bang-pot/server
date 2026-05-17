package com.banglog.meeting.application.port;

import java.time.Instant;
import java.util.Optional;

import com.banglog.meeting.domain.MeetingParticipant;

public interface MeetingParticipantRepository {

	MeetingParticipant save(MeetingParticipant participant);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	int leaveInactiveCrewMemberParticipations(Long crewId, Long userId, Instant updatedAt);

	long countByMeetingId(Long meetingId);

	void delete(MeetingParticipant participant);
}
