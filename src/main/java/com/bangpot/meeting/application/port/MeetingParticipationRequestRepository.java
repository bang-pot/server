package com.bangpot.meeting.application.port;

import java.util.Optional;

import com.bangpot.meeting.domain.MeetingParticipationRequest;

public interface MeetingParticipationRequestRepository {

	MeetingParticipationRequest save(MeetingParticipationRequest request);

	Optional<MeetingParticipationRequest> findByMeetingIdAndUserId(Long meetingId, Long userId);
}
