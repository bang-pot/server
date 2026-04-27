package com.bangpot.meeting.infrastructure;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;

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
	public boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<MeetingStatus> statuses) {
		return meetingJpaRepository.existsByCrewIdAndHostUserIdAndStatusIn(crewId, hostUserId, statuses);
	}

	@Override
	public boolean existsByCrewIdAndStatusIn(Long crewId, List<MeetingStatus> statuses) {
		return meetingJpaRepository.existsByCrewIdAndStatusIn(crewId, statuses);
	}
}
