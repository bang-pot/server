package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

interface JpaProfileHubReadRepository extends Repository<UserJpaEntity, Long> {

	List<MeetingParticipationStatus> JOINED_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	@Query("""
		select count(m)
		from Meeting m
		where m.hostUserId = :userId
		""")
	Long countCreatedMeetings(@Param("userId") Long userId);

	@Query("""
		select count(mp)
		from MeetingParticipant mp, Meeting m
		where mp.meetingId = m.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.hostUserId <> :userId
		""")
	Long countJoinedMeetings(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses
	);

	@Query("""
		select count(cm)
		from CrewMember cm, Crew c
		where cm.crewId = c.id
		  and cm.userId = :userId
		  and cm.status = :activeStatus
		  and c.status = :activeCrewStatus
		""")
	Long countMyCrews(
		@Param("userId") Long userId,
		@Param("activeStatus") CrewMemberStatus activeStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus
	);

	@Query("""
		select count(cjr)
		from CrewJoinRequest cjr, Crew c
		where cjr.userId = :userId
		  and cjr.crewId = c.id
		  and cjr.status = :pendingStatus
		  and c.status = :activeCrewStatus
		  and c.visibility = :publicVisibility
		""")
	Long countPendingCrews(
		@Param("userId") Long userId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility
	);
}
