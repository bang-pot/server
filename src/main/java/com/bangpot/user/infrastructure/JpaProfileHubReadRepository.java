package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.user.application.port.ProfileHubReadRepository;

interface JpaProfileHubReadRepository extends Repository<UserJpaEntity, Long>, ProfileHubReadRepository {

	List<MeetingParticipationStatus> JOINED_STATUSES = List.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	@Override
	default Counts loadCounts(Long userId) {
		return Counts.of(
			countCreatedMeetings(userId),
			countJoinedMeetings(userId, JOINED_STATUSES),
			countMyCrews(userId, CrewMemberStatus.ACTIVE),
			countPendingCrews(userId, CrewJoinRequestStatus.PENDING)
		);
	}

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
		from CrewMember cm
		where cm.userId = :userId
		  and cm.status = :activeStatus
		""")
	Long countMyCrews(
		@Param("userId") Long userId,
		@Param("activeStatus") CrewMemberStatus activeStatus
	);

	@Query("""
		select count(cjr)
		from CrewJoinRequest cjr
		where cjr.userId = :userId
		  and cjr.status = :pendingStatus
		""")
	Long countPendingCrews(
		@Param("userId") Long userId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus
	);
}
