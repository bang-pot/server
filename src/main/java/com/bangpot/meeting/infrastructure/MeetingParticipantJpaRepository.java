package com.bangpot.meeting.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	@Modifying(flushAutomatically = true)
	@Query("""
		update MeetingParticipant mp
		set mp.status = :leftStatus,
		    mp.updatedAt = :updatedAt
		where mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and exists (
			select 1
			from Meeting m
			where m.id = mp.meetingId
			  and m.crewId = :crewId
			  and m.hostUserId <> :userId
			  and m.status in :unfinishedMeetingStatuses
		  )
		""")
	int leaveInactiveCrewMemberParticipations(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("unfinishedMeetingStatuses") List<MeetingStatus> unfinishedMeetingStatuses,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses,
		@Param("leftStatus") MeetingParticipationStatus leftStatus,
		@Param("updatedAt") Instant updatedAt
	);

	long countByMeetingIdAndStatusIn(Long meetingId, List<MeetingParticipationStatus> statuses);
}
