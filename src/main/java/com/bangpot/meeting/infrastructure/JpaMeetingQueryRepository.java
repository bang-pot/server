package com.bangpot.meeting.infrastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaMeetingQueryRepository implements MeetingQueryRepository {

	private static final List<MeetingStatus> INCLUDED_CREATED_MEETING_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED,
		MeetingStatus.COMPLETED,
		MeetingStatus.CANCELED
	);

	private static final List<MeetingParticipationStatus> JOINED_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	private final MeetingJpaRepository meetingJpaRepository;

	@Override
	public MyCalendarView findMyCalendarViewByUserId(Long userId) {
		List<MyCalendarView.Item> hostedItems = meetingJpaRepository.findMyHostedCalendarItemsByUserId(
			userId,
			CrewStatus.ACTIVE,
			INCLUDED_CREATED_MEETING_STATUSES,
			MeetingStatus.CANCELED
		);
		List<MyCalendarView.Item> participatingItems = meetingJpaRepository.findMyParticipatingCalendarItemsByUserId(
			userId,
			List.of(
				MeetingParticipationStatus.JOINED,
				MeetingParticipationStatus.APPROVED
			),
			CrewStatus.ACTIVE,
			INCLUDED_CREATED_MEETING_STATUSES,
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
	public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
		org.springframework.data.domain.Slice<MyCreatedMeetingsView.Item> slice =
			meetingJpaRepository.findMyCreatedMeetingsViewByHostUserId(
			userId,
			CrewStatus.ACTIVE,
			INCLUDED_CREATED_MEETING_STATUSES,
			PageRequest.of(page, size)
			);
		return MyCreatedMeetingsView.of(slice.getContent(), MyCreatedMeetingsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
		org.springframework.data.domain.Slice<MyJoinedMeetingsView.Item> slice =
			meetingJpaRepository.findMyJoinedMeetingsViewByUserId(
			userId,
			JOINED_STATUSES,
			CrewStatus.ACTIVE,
			INCLUDED_CREATED_MEETING_STATUSES,
			MeetingStatus.COMPLETED,
			PageRequest.of(page, size)
			);
		return MyJoinedMeetingsView.of(slice.getContent(), MyJoinedMeetingsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public long countCreatedByHostUserId(Long userId) {
		return meetingJpaRepository.countByHostUserId(userId);
	}

	@Override
	public long countJoinedByUserId(Long userId) {
		return meetingJpaRepository.countJoinedByUserId(userId, JOINED_STATUSES);
	}
}
