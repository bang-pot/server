package com.bangpot.meeting.application.port;

import java.util.Optional;

import com.bangpot.meeting.domain.MeetingParticipant;

public interface MeetingParticipantRepository {

	MeetingParticipant save(MeetingParticipant participant);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	long countByMeetingId(Long meetingId);

	void delete(MeetingParticipant participant);
}
