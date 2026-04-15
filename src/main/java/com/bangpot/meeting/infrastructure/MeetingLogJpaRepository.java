package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.meeting.domain.MeetingLog;

interface MeetingLogJpaRepository extends JpaRepository<MeetingLog, Long> {

	Optional<MeetingLog> findByIdAndDeletedAtIsNull(Long id);

	Optional<MeetingLog> findByMeetingIdAndAuthorUserIdAndDeletedAtIsNull(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserIdAndDeletedAtIsNotNull(Long meetingId, Long authorUserId);
}
