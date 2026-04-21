package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {

	List<Meeting> findAllByCrewIdOrderByMeetingDateAscMeetingTimeAscIdAsc(Long crewId);

	Optional<Meeting> findByIdAndCrewId(Long id, Long crewId);

	@Query("""
		select count(m)
		from Meeting m
		where m.hostUserId = :userId
		""")
	long countByHostUserId(@Param("userId") Long userId);

	@Query("""
		select count(mp)
		from MeetingParticipant mp, Meeting m
		where mp.meetingId = m.id
		  and mp.userId = :userId
		  and mp.status in :joinedStatuses
		  and m.hostUserId <> :userId
		""")
	long countJoinedByUserId(
		@Param("userId") Long userId,
		@Param("joinedStatuses") List<MeetingParticipationStatus> joinedStatuses
	);

	boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<MeetingStatus> statuses);

	boolean existsByCrewIdAndStatusIn(Long crewId, List<MeetingStatus> statuses);
}
