package com.banglog.meeting.application.port;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.meeting.domain.view.MyCalendarView;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;

public interface MeetingRepository {

	Meeting save(Meeting meeting);

	List<Meeting> findAllByCrewId(Long crewId);

	List<Meeting> findRecruitmentCloseTargets(LocalDateTime now, int limit);

	List<Meeting> findCompletionTargets(LocalDateTime completionCutoff, int limit);

	int cancelUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId, Instant updatedAt);

	Optional<Meeting> findById(Long meetingId);

	Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId);

	Optional<Meeting> findByIdAndCrewIdForUpdate(Long meetingId, Long crewId);

	MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size);

	MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size);

	MyCalendarView findMyCalendarViewByUserId(Long userId);

	long countCreatedByHostUserId(Long userId);

	long countJoinedByUserId(Long userId);

	boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId);

	boolean existsUnfinishedByCrewId(Long crewId);
}
