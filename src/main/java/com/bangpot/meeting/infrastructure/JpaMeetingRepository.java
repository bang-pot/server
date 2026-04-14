package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingRepository implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;

	@Override
	public Meeting save(Meeting meeting) {
		return meetingJpaRepository.save(meeting);
	}

	@Override
	public List<Meeting> findAllByCrewId(Long crewId) {
		return meetingJpaRepository.findAllByCrewIdOrderByMeetingDateAscMeetingTimeAscIdAsc(crewId);
	}

	@Override
	public Optional<Meeting> findById(Long meetingId) {
		return meetingJpaRepository.findById(meetingId);
	}

	@Override
	public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
		return meetingJpaRepository.findByIdAndCrewId(meetingId, crewId);
	}

	@Override
	public boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<MeetingStatus> statuses) {
		return meetingJpaRepository.existsByCrewIdAndHostUserIdAndStatusIn(crewId, hostUserId, statuses);
	}

	@Override
	public boolean existsByCrewIdAndStatusIn(Long crewId, List<MeetingStatus> statuses) {
		return meetingJpaRepository.existsByCrewIdAndStatusIn(crewId, statuses);
	}
}
