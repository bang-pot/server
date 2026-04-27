package com.bangpot.meeting.application.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.bangpot.meeting.domain.view.CrewScheduleView;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryView;
import com.bangpot.meeting.domain.view.MeetingDetailView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MeetingsView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;

public interface MeetingQueryRepository {

	MyCalendarView findMyCalendarViewByUserId(Long userId);

	MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size);

	MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size);

	UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
		Long userId,
		int limit,
		String currentDate,
		String currentTime
	);

	CrewScheduleView findCrewScheduleViewByCrewId(Long crewId, LocalDate from, LocalDate to);

	CrewMeetingGalleryView findCrewMeetingGalleryView(Long crewId, int page, int size);

	Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(Long crewId, Long userId);

	MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size);

	Optional<MeetingDetailView> findMeetingDetailView(
		Long crewId,
		Long meetingId,
		Long userId
	);

	long countCreatedByHostUserId(Long userId);

	long countJoinedByUserId(Long userId);

	Map<Long, Integer> countCompletedByUserIds(Collection<Long> userIds);
}
