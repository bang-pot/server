package com.bangpot.gallerylog.application.port;

import java.util.Optional;

import com.bangpot.gallerylog.domain.MeetingLog;

public interface MeetingLogRepository {

	MeetingLog save(MeetingLog log);

	Optional<MeetingLog> findById(Long logId);

	Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	void delete(MeetingLog log);
}
