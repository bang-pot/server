package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {

	List<Meeting> findAllByCrewIdOrderByMeetingDateAscMeetingTimeAscIdAsc(Long crewId);

	Optional<Meeting> findByIdAndCrewId(Long id, Long crewId);

	boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<MeetingStatus> statuses);
}
