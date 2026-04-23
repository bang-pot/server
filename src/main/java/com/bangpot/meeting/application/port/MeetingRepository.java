package com.bangpot.meeting.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;

public interface MeetingRepository {

	Meeting save(Meeting meeting);

	List<Meeting> findAllByCrewId(Long crewId);

	Optional<Meeting> findById(Long meetingId);

	Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId);

	default MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
		return MyCreatedMeetingsView.of(List.of(), MyCreatedMeetingsView.Page.of(page, size, false));
	}

	default MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
		return MyJoinedMeetingsView.of(List.of(), MyJoinedMeetingsView.Page.of(page, size, false));
	}

	default MyCalendarView findMyCalendarViewByUserId(Long userId) {
		return MyCalendarView.of(List.of(), 0);
	}

	long countCreatedByHostUserId(Long userId);

	long countJoinedByUserId(Long userId);

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
