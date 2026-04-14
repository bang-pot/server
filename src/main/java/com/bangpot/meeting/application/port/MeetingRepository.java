package com.bangpot.meeting.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

public interface MeetingRepository {

	Meeting save(Meeting meeting);

	List<Meeting> findAllByCrewId(Long crewId);

	Optional<Meeting> findById(Long meetingId);

	Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId);

	default boolean existsByCrewIdAndHostUserIdAndStatusIn(
		Long crewId,
		Long hostUserId,
		List<MeetingStatus> statuses
	) {
		return false;
	}

	default boolean existsByCrewIdAndStatusIn(Long crewId, List<MeetingStatus> statuses) {
		return false;
	}
}
