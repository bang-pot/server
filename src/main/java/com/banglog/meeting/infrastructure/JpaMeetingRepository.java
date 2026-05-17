package com.banglog.meeting.infrastructure;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.banglog.crew.domain.CrewStatus;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.meeting.domain.MeetingResult;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.meeting.domain.view.MyCalendarView;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingRepository implements MeetingRepository {

	private static final List<MeetingStatus> INCLUDED_MEETING_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED,
		MeetingStatus.COMPLETED,
		MeetingStatus.CANCELED
	);

	private static final List<MeetingParticipationStatus> CONFIRMED_PARTICIPATION_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.APPROVED
	);

	private static final List<MeetingParticipationStatus> JOINED_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	private static final List<MeetingStatus> UNFINISHED_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED
	);
	private static final DateTimeFormatter MEETING_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter MEETING_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
	public List<Meeting> findRecruitmentCloseTargets(LocalDateTime now, int limit) {
		return meetingJpaRepository.findDueMeetingsByStatus(
			MeetingStatus.RECRUITING,
			formatDate(now),
			formatTime(now),
			PageRequest.of(0, limit)
		);
	}

	@Override
	public List<Meeting> findCompletionTargets(LocalDateTime completionCutoff, int limit) {
		return meetingJpaRepository.findDueMeetingsByStatus(
			MeetingStatus.RECRUITMENT_CLOSED,
			formatDate(completionCutoff),
			formatTime(completionCutoff),
			PageRequest.of(0, limit)
		);
	}

	private String formatDate(LocalDateTime dateTime) {
		return dateTime.format(MEETING_DATE_FORMATTER);
	}

	private String formatTime(LocalDateTime dateTime) {
		return dateTime.format(MEETING_TIME_FORMATTER);
	}

	@Override
	public int cancelUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId, Instant updatedAt) {
		return meetingJpaRepository.cancelUnfinishedByCrewIdAndHostUserId(
			crewId,
			hostUserId,
			UNFINISHED_STATUSES,
			MeetingStatus.CANCELED,
			updatedAt
		);
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
	public Optional<Meeting> findByIdAndCrewIdForUpdate(Long meetingId, Long crewId) {
		return meetingJpaRepository.findByIdAndCrewIdForUpdate(meetingId, crewId);
	}

	@Override
	public int recordResultIfNotRecorded(
		Long meetingId,
		Long crewId,
		Long hostUserId,
		MeetingResult result,
		Instant updatedAt
	) {
		return meetingJpaRepository.recordResultIfNotRecorded(
			meetingId,
			crewId,
			hostUserId,
			MeetingStatus.COMPLETED,
			MeetingResult.NOT_RECORDED,
			result,
			updatedAt
		);
	}

	@Override
	public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
		Slice<MyCreatedMeetingsView.Item> slice = meetingJpaRepository.findMyCreatedMeetingsViewByHostUserId(
			userId,
			CrewStatus.ACTIVE,
			List.of(
				MeetingStatus.RECRUITING,
				MeetingStatus.RECRUITMENT_CLOSED,
				MeetingStatus.COMPLETED,
				MeetingStatus.CANCELED
			),
			PageRequest.of(page, size)
		);
		return MyCreatedMeetingsView.of(slice.getContent(), MyCreatedMeetingsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
		Slice<MyJoinedMeetingsView.Item> slice = meetingJpaRepository.findMyJoinedMeetingsViewByUserId(
			userId,
			JOINED_STATUSES,
			CrewStatus.ACTIVE,
			List.of(
				MeetingStatus.RECRUITING,
				MeetingStatus.RECRUITMENT_CLOSED,
				MeetingStatus.COMPLETED,
				MeetingStatus.CANCELED
			),
			MeetingStatus.COMPLETED,
			PageRequest.of(page, size)
		);
		return MyJoinedMeetingsView.of(slice.getContent(), MyJoinedMeetingsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public MyCalendarView findMyCalendarViewByUserId(Long userId) {
		List<MyCalendarView.Item> hostedItems = meetingJpaRepository.findMyHostedCalendarItemsByUserId(
			userId,
			CrewStatus.ACTIVE,
			INCLUDED_MEETING_STATUSES,
			MeetingStatus.CANCELED
		);
		List<MyCalendarView.Item> participatingItems = meetingJpaRepository.findMyParticipatingCalendarItemsByUserId(
			userId,
			CONFIRMED_PARTICIPATION_STATUSES,
			CrewStatus.ACTIVE,
			INCLUDED_MEETING_STATUSES,
			MeetingStatus.CANCELED
		);

		List<MyCalendarView.Item> mergedItems = new ArrayList<>(hostedItems.size() + participatingItems.size());
		mergedItems.addAll(hostedItems);
		mergedItems.addAll(participatingItems);

		LinkedHashMap<Long, MyCalendarView.Item> uniqueItemsByMeetingId = new LinkedHashMap<>();
		for (MyCalendarView.Item item : mergedItems) {
			uniqueItemsByMeetingId.putIfAbsent(item.meetingId(), item);
		}

		List<MyCalendarView.Item> items = new ArrayList<>(uniqueItemsByMeetingId.values());
		items.sort(java.util.Comparator
			.comparing(MyCalendarView.Item::date)
			.thenComparing(MyCalendarView.Item::time)
			.thenComparing(MyCalendarView.Item::meetingId));
		return MyCalendarView.of(items, items.size());
	}

	@Override
	public long countCreatedByHostUserId(Long userId) {
		return meetingJpaRepository.countByHostUserId(userId);
	}

	@Override
	public long countJoinedByUserId(Long userId) {
		return meetingJpaRepository.countJoinedByUserId(userId, JOINED_STATUSES);
	}

	@Override
	public boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId) {
		return meetingJpaRepository.existsByCrewIdAndHostUserIdAndStatusIn(
			crewId,
			hostUserId,
			UNFINISHED_STATUSES
		);
	}

	@Override
	public boolean existsUnfinishedByCrewId(Long crewId) {
		return meetingJpaRepository.existsByCrewIdAndStatusIn(crewId, UNFINISHED_STATUSES);
	}
}
