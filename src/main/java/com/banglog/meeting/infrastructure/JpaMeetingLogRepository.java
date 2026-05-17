package com.banglog.meeting.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.banglog.meeting.application.port.MeetingLogRepository;
import com.banglog.meeting.domain.MeetingLog;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingLogRepository implements MeetingLogRepository {

	private final MeetingLogJpaRepository meetingLogJpaRepository;

	@Override
	public MeetingLog save(MeetingLog log) {
		return meetingLogJpaRepository.saveAndFlush(log);
	}

	@Override
	public Optional<MeetingLog> findById(Long logId) {
		return meetingLogJpaRepository.findByIdAndDeletedAtIsNull(logId);
	}

	@Override
	public Optional<MeetingLog> findByIdForUpdate(Long logId) {
		return meetingLogJpaRepository.findActiveByIdForUpdate(logId);
	}

	@Override
	public Optional<MeetingLog> findActiveLogInCrewForUpdate(Long crewId, Long logId) {
		return meetingLogJpaRepository.findActiveLogInCrewForUpdate(crewId, logId);
	}

	@Override
	public Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
		return meetingLogJpaRepository.findByMeetingIdAndAuthorUserIdAndDeletedAtIsNull(meetingId, authorUserId);
	}

	@Override
	public boolean existsAnyByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
		return meetingLogJpaRepository.existsByMeetingIdAndAuthorUserId(meetingId, authorUserId);
	}

	@Override
	public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
		return meetingLogJpaRepository.existsByMeetingIdAndAuthorUserIdAndDeletedAtIsNotNull(meetingId, authorUserId);
	}
}
