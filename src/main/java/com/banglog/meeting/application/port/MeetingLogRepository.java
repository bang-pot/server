package com.banglog.meeting.application.port;

import java.util.Optional;

import com.banglog.meeting.domain.MeetingLog;

public interface MeetingLogRepository {

	MeetingLog save(MeetingLog log);

	Optional<MeetingLog> findById(Long logId);

	Optional<MeetingLog> findByIdForUpdate(Long logId);

	Optional<MeetingLog> findActiveLogInCrewForUpdate(Long crewId, Long logId);

	Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsAnyByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);
}
