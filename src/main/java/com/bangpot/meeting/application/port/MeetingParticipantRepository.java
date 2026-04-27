package com.bangpot.meeting.application.port;

import java.time.Instant;
import java.util.Optional;

import com.bangpot.meeting.domain.MeetingParticipant;

public interface MeetingParticipantRepository {

	MeetingParticipant save(MeetingParticipant participant);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	int leaveJoinedByCrewIdAndUserIdInUnfinishedMeetings(Long crewId, Long userId, Instant updatedAt);

	long countByMeetingId(Long meetingId);

	void delete(MeetingParticipant participant);
}
