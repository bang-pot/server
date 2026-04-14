package com.bangpot.gallerylog.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.gallerylog.domain.MeetingLog;

interface MeetingLogJpaRepository extends JpaRepository<MeetingLog, Long> {

	Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);

	boolean existsByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId);
}
