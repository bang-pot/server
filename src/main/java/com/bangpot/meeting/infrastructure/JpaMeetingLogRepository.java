package com.bangpot.meeting.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.domain.MeetingLog;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingLogRepository implements MeetingLogRepository {

	private final MeetingLogJpaRepository meetingLogJpaRepository;

	@Override
	public MeetingLog save(MeetingLog log) {
		return meetingLogJpaRepository.save(log);
	}

	@Override
	public Optional<MeetingLog> findById(Long logId) {
		return meetingLogJpaRepository.findById(logId);
	}

	@Override
	public Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
		return meetingLogJpaRepository.findByMeetingIdAndAuthorUserId(meetingId, authorUserId);
	}

	@Override
	public boolean existsByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
		return meetingLogJpaRepository.existsByMeetingIdAndAuthorUserId(meetingId, authorUserId);
	}

	@Override
	public void delete(MeetingLog log) {
		meetingLogJpaRepository.delete(log);
	}
}

