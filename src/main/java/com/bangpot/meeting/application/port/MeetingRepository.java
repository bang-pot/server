package com.bangpot.meeting.application.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;

public interface MeetingRepository {

	Meeting save(Meeting meeting);

	List<Meeting> findAllByCrewId(Long crewId);

	int cancelUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId, Instant updatedAt);

	Optional<Meeting> findById(Long meetingId);

	Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId);

	MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size);

	MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size);

	MyCalendarView findMyCalendarViewByUserId(Long userId);

	long countCreatedByHostUserId(Long userId);

	long countJoinedByUserId(Long userId);

	boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId);

	boolean existsUnfinishedByCrewId(Long crewId);
}
