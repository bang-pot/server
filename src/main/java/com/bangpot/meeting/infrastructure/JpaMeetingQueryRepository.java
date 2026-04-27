package com.bangpot.meeting.infrastructure;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryView;
import com.bangpot.meeting.domain.view.CrewScheduleView;
import com.bangpot.meeting.domain.view.MeetingDetailView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MeetingsView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;

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

	private static final List<MeetingParticipationStatus> JOINED_STATUSES = Arrays.stream(
		MeetingParticipationStatus.values()
	)
		.filter(MeetingParticipationStatus::representsJoined)
		.toList();

	private final MeetingJpaRepository meetingJpaRepository;
	private final MeetingGalleryJpaRepository meetingGalleryJpaRepository;

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
		items.sort(Comparator
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
	public UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
		Long userId,
		int limit,
		String currentDate,
		String currentTime
	) {
		List<UpcomingMeetingsView.Item> items = meetingJpaRepository.findUpcomingMeetingsViewByUserId(
			userId,
			CrewStatus.ACTIVE,
			List.of(MeetingStatus.RECRUITING, MeetingStatus.RECRUITMENT_CLOSED),
			JOINED_STATUSES,
			currentDate,
			currentTime,
			PageRequest.of(0, limit)
		);
		long totalCount = meetingJpaRepository.countUpcomingMeetingsByUserId(
			userId,
			CrewStatus.ACTIVE,
			List.of(MeetingStatus.RECRUITING, MeetingStatus.RECRUITMENT_CLOSED),
			JOINED_STATUSES,
			currentDate,
			currentTime
		);
		return UpcomingMeetingsView.of(items, totalCount);
	}

	@Override
	public CrewScheduleView findCrewScheduleViewByCrewId(Long crewId, LocalDate from, LocalDate to) {
		return CrewScheduleView.of(meetingJpaRepository.findCrewScheduleItemsByCrewId(
			crewId,
			INCLUDED_CREATED_MEETING_STATUSES,
			MeetingStatus.RECRUITING,
			MeetingStatus.CANCELED,
			JOINED_STATUSES,
			from.toString(),
			to.toString()
		));
	}

	@Override
	public CrewMeetingGalleryView findCrewMeetingGalleryView(Long crewId, int page, int size) {
		List<CrewMeetingGalleryView.Item> items = meetingGalleryJpaRepository.findGalleryCards(
				crewId,
				PageRequest.of(page, size + 1)
			).stream()
			.map(row -> CrewMeetingGalleryView.Item.of(
				toLong(row.getMeetingId()),
				row.getMeetingDate(),
				row.getMeetingTitle(),
				row.getCoverPhotoUrl(),
				toLong(row.getExtraPhotoCount())
			))
			.toList();

		boolean hasNext = items.size() > size;
		if (hasNext) {
			items = items.subList(0, size);
		}
		return CrewMeetingGalleryView.of(items, CrewMeetingGalleryView.Page.of(page, size, hasNext));
	}

	@Override
	public Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(Long crewId, Long userId) {
		return meetingJpaRepository.findMeetingsAccessViewByCrewIdAndUserId(
			crewId,
			userId,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE
		);
	}

	@Override
	public MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
		Slice<MeetingsView.Item> slice = meetingJpaRepository.findMeetingItemsByCrewId(
			crewId,
			INCLUDED_CREATED_MEETING_STATUSES,
			PageRequest.of(page, size)
		);
		return MeetingsView.of(
			slice.getContent(),
			MeetingsView.Page.of(page, size, slice.hasNext())
		);
	}

	@Override
	public Optional<MeetingDetailView> findMeetingDetailView(
		Long crewId,
		Long meetingId,
		Long userId
	) {
		return meetingJpaRepository.findMeetingDetailView(
			crewId,
			meetingId,
			userId,
			JOINED_STATUSES
		);
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
	public Map<Long, Integer> countCompletedByUserIds(Collection<Long> userIds) {
		if (userIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, Integer> countsByUserId = new HashMap<>();
		meetingJpaRepository.countCompletedHostedMeetingsByUserIds(userIds, MeetingStatus.COMPLETED)
			.forEach(row -> countsByUserId.put(row.getUserId(), Math.toIntExact(row.getMeetingCount())));
		meetingJpaRepository.countCompletedJoinedMeetingsByUserIds(
			userIds,
			JOINED_STATUSES,
			MeetingStatus.COMPLETED
		).forEach(row -> countsByUserId.merge(row.getUserId(), Math.toIntExact(row.getMeetingCount()), Integer::sum));
		return countsByUserId;
	}

	private Long toLong(Number value) {
		if (value == null) {
			return null;
		}
		return value.longValue();
	}
}
