package com.bangpot.user.infrastructure;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.port.CalendarReadRepository;

interface JpaCalendarReadRepository extends Repository<UserJpaEntity, Long>, CalendarReadRepository {

	List<MeetingStatus> INCLUDED_MEETING_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED,
		MeetingStatus.COMPLETED,
		MeetingStatus.CANCELED
	);

	List<MeetingParticipationStatus> CONFIRMED_PARTICIPATION_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.APPROVED
	);

	Comparator<Item> ITEM_ORDER = Comparator
		.comparing(Item::date)
		.thenComparing(Item::time)
		.thenComparing(Item::meetingId);

	@Override
	default View load(Long userId) {
		List<Item> items = mergeItems(
			loadHostedMeetings(userId).stream()
				.map(row -> Item.of(
					row.getMeetingId(),
					row.getMeetingTitle(),
					row.getCrewId(),
					row.getCrewName(),
					row.getDate(),
					row.getTime(),
					row.getMeetingStatus().name(),
					row.getMeetingStatus() == MeetingStatus.CANCELED,
					"HOST"
				))
				.toList(),
			loadParticipatingMeetings(userId).stream()
				.map(row -> Item.of(
					row.getMeetingId(),
					row.getMeetingTitle(),
					row.getCrewId(),
					row.getCrewName(),
					row.getDate(),
					row.getTime(),
					row.getMeetingStatus().name(),
					row.getMeetingStatus() == MeetingStatus.CANCELED,
					"PARTICIPANT"
				))
				.toList()
		);
		return View.of(items, items.size());
	}

	private List<Item> mergeItems(List<Item> hostedMeetings, List<Item> participatingMeetings) {
		return java.util.stream.Stream.concat(hostedMeetings.stream(), participatingMeetings.stream())
			.distinct()
			.sorted(ITEM_ORDER)
			.toList();
	}

	@Query("""
		select
			m.id as meetingId,
			m.title as meetingTitle,
			c.id as crewId,
			c.name as crewName,
			m.status as meetingStatus,
			m.meetingDate as date,
			m.meetingTime as time
		from Meeting m, Crew c
		where m.crewId = c.id
		  and m.hostUserId = :userId
		  and c.status = :activeCrewStatus
		  and m.status in :includedMeetingStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<Row> loadHostedMeetings(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedMeetingStatuses") List<MeetingStatus> includedMeetingStatuses
	);

	default List<Row> loadHostedMeetings(Long userId) {
		return loadHostedMeetings(userId, CrewStatus.ACTIVE, INCLUDED_MEETING_STATUSES);
	}

	@Query("""
		select
			m.id as meetingId,
			m.title as meetingTitle,
			c.id as crewId,
			c.name as crewName,
			m.status as meetingStatus,
			m.meetingDate as date,
			m.meetingTime as time
		from MeetingParticipant mp, Meeting m, Crew c
		where mp.meetingId = m.id
		  and m.crewId = c.id
		  and mp.userId = :userId
		  and mp.status in :confirmedParticipantStatuses
		  and m.hostUserId <> :userId
		  and c.status = :activeCrewStatus
		  and m.status in :includedMeetingStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<Row> loadParticipatingMeetings(
		@Param("userId") Long userId,
		@Param("confirmedParticipantStatuses") List<MeetingParticipationStatus> confirmedParticipantStatuses,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("includedMeetingStatuses") List<MeetingStatus> includedMeetingStatuses
	);

	default List<Row> loadParticipatingMeetings(Long userId) {
		return loadParticipatingMeetings(
			userId,
			CONFIRMED_PARTICIPATION_STATUSES,
			CrewStatus.ACTIVE,
			INCLUDED_MEETING_STATUSES
		);
	}

	interface Row {
		Long getMeetingId();
		String getMeetingTitle();
		Long getCrewId();
		String getCrewName();
		MeetingStatus getMeetingStatus();
		String getDate();
		String getTime();
	}
}
