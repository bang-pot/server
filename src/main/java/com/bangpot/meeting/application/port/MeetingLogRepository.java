package com.bangpot.meeting.application.port;

import java.util.Optional;

import com.bangpot.meeting.domain.MeetingLog;

public interface MeetingLogRepository {

	MeetingLog save(MeetingLog log);

	Optional<MeetingLog> findById(Long logId);

	Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsAnyByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);
}
