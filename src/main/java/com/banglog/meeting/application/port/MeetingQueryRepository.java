package com.banglog.meeting.application.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.banglog.meeting.domain.view.CrewScheduleView;
import com.banglog.meeting.domain.view.CrewMeetingGalleryDetailView;
import com.banglog.meeting.domain.view.CrewMeetingGalleryDetailTargetView;
import com.banglog.meeting.domain.view.CrewMeetingGalleryView;
import com.banglog.meeting.domain.view.MeetingDetailView;
import com.banglog.meeting.domain.view.MeetingActivityRecordView;
import com.banglog.meeting.domain.view.MeetingsAccessView;
import com.banglog.meeting.domain.view.MeetingsView;
import com.banglog.meeting.domain.view.MyCalendarView;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.meeting.domain.view.UpcomingMeetingsView;

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

	MeetingActivityRecordView findActivityRecordViewByUserId(Long userId);

	CrewScheduleView findCrewScheduleViewByCrewId(Long crewId, LocalDate from, LocalDate to);

	CrewMeetingGalleryView findCrewMeetingGalleryView(Long crewId, int page, int size);

	Optional<CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(Long crewId, Long meetingId);

	List<CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(Long meetingId);

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
