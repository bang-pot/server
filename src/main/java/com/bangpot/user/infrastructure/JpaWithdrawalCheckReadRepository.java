package com.bangpot.user.infrastructure;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.port.WithdrawalCheckReadRepository;

interface JpaWithdrawalCheckReadRepository extends Repository<UserJpaEntity, Long>, WithdrawalCheckReadRepository {

	List<MeetingStatus> BLOCKING_MEETING_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED
	);

	List<MeetingParticipationStatus> BLOCKING_PARTICIPATION_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.APPROVED
	);

	Comparator<ParticipatingMeeting> PARTICIPATING_MEETING_ORDER = Comparator
		.comparing(ParticipatingMeeting::date)
		.thenComparing(ParticipatingMeeting::time)
		.thenComparing(ParticipatingMeeting::meetingId);

	@Override
	default View load(Long userId) {
		List<ParticipatingMeeting> blockingParticipatingMeetings = mergeBlockingMeetings(
			loadBlockingHostedMeetings(userId).stream()
				.map(row -> ParticipatingMeeting.of(
					row.getMeetingId(),
					row.getMeetingTitle(),
					row.getCrewId(),
					row.getCrewName(),
					row.getMeetingStatus().name(),
					row.getDate(),
					row.getTime(),
					"HOST"
				))
				.toList(),
			loadBlockingJoinedMeetings(userId).stream()
				.map(row -> ParticipatingMeeting.of(
					row.getMeetingId(),
					row.getMeetingTitle(),
					row.getCrewId(),
					row.getCrewName(),
					row.getMeetingStatus().name(),
					row.getDate(),
					row.getTime(),
					"PARTICIPANT"
				))
				.toList()
		);

		return View.of(
			loadBlockingActiveCrews(userId).stream()
				.map(row -> ActiveCrew.of(row.getCrewId(), row.getCrewName()))
				.toList(),
			blockingParticipatingMeetings
		);
	}

	private List<ParticipatingMeeting> mergeBlockingMeetings(
		List<ParticipatingMeeting> hostedMeetings,
		List<ParticipatingMeeting> joinedMeetings
	) {
		return java.util.stream.Stream.concat(hostedMeetings.stream(), joinedMeetings.stream())
			.distinct()
			.sorted(PARTICIPATING_MEETING_ORDER)
			.toList();
	}

	@Query("""
		select
			c.id as crewId,
			c.name as crewName
		from CrewMember cm, Crew c
		where cm.crewId = c.id
		  and cm.userId = :userId
		  and cm.status = :activeMemberStatus
		  and c.status = :activeCrewStatus
		order by c.name asc, c.id asc
		""")
	List<ActiveCrewRow> loadBlockingActiveCrews(
		@Param("userId") Long userId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus
	);

	default List<ActiveCrewRow> loadBlockingActiveCrews(Long userId) {
		return loadBlockingActiveCrews(userId, CrewMemberStatus.ACTIVE, CrewStatus.ACTIVE);
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
		  and m.status in :blockingStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<ParticipatingMeetingRow> loadBlockingHostedMeetings(
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("blockingStatuses") List<MeetingStatus> blockingStatuses
	);

	default List<ParticipatingMeetingRow> loadBlockingHostedMeetings(Long userId) {
		return loadBlockingHostedMeetings(userId, CrewStatus.ACTIVE, BLOCKING_MEETING_STATUSES);
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
		  and mp.status in :blockingParticipantStatuses
		  and m.hostUserId <> :userId
		  and c.status = :activeCrewStatus
		  and m.status in :blockingMeetingStatuses
		order by m.meetingDate asc, m.meetingTime asc, m.id asc
		""")
	List<ParticipatingMeetingRow> loadBlockingJoinedMeetings(
		@Param("userId") Long userId,
		@Param("blockingParticipantStatuses") List<MeetingParticipationStatus> blockingParticipantStatuses,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("blockingMeetingStatuses") List<MeetingStatus> blockingMeetingStatuses
	);

	default List<ParticipatingMeetingRow> loadBlockingJoinedMeetings(Long userId) {
		return loadBlockingJoinedMeetings(
			userId,
			BLOCKING_PARTICIPATION_STATUSES,
			CrewStatus.ACTIVE,
			BLOCKING_MEETING_STATUSES
		);
	}

	interface ActiveCrewRow {
		Long getCrewId();
		String getCrewName();
	}

	interface ParticipatingMeetingRow {
		Long getMeetingId();
		String getMeetingTitle();
		Long getCrewId();
		String getCrewName();
		MeetingStatus getMeetingStatus();
		String getDate();
		String getTime();
	}
}
