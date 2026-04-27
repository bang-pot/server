package com.bangpot.crew.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.bangpot.crew.domain.view.CrewJoinRequestsView;
import com.bangpot.crew.domain.view.PendingCrewJoinRequestsView;

import jakarta.persistence.LockModeType;

interface CrewJoinRequestJpaRepository extends JpaRepository<CrewJoinRequest, Long> {

	boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewJoinRequestStatus status);

	List<CrewJoinRequest> findAllByCrewIdOrderByIdAsc(Long crewId);

	List<CrewJoinRequest> findAllByCrewIdAndStatusOrderByIdAsc(Long crewId, CrewJoinRequestStatus status);

	Optional<CrewJoinRequest> findByIdAndCrewIdAndStatus(Long id, Long crewId, CrewJoinRequestStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<CrewJoinRequest> findWithLockByIdAndCrewIdAndStatus(
		Long id,
		Long crewId,
		CrewJoinRequestStatus status
	);

	Optional<CrewJoinRequest> findByIdAndUserIdAndStatus(Long id, Long userId, CrewJoinRequestStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<CrewJoinRequest> findWithLockByIdAndUserIdAndStatus(
		Long id,
		Long userId,
		CrewJoinRequestStatus status
	);

	@Query("""
		select new com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView(
			member.role
		)
		from Crew c
		left join CrewMember member
		  on member.crewId = c.id
		 and member.userId = :userId
		 and member.status = :activeMemberStatus
		where c.id = :crewId
		  and c.status = :activeCrewStatus
		""")
	Optional<CrewJoinRequestManagementAccessView> findManagementAccessByCrewIdAndUserId(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus
	);

	@Query("""
		select new com.bangpot.crew.domain.view.CrewJoinRequestsView$Item(
			request.id,
			user.id,
			user.nickname,
			request.message,
			case
				when request.status = :pendingStatus then 'PENDING'
				when request.status = :approvedStatus then 'APPROVED'
				when request.status = :rejectedStatus then 'REJECTED'
				else 'CANCELED'
			end
		)
		from CrewJoinRequest request, UserJpaEntity user
		where request.userId = user.id
		  and request.crewId = :crewId
		  and user.withdrawnAt is null
		order by request.id asc
		""")
	Slice<CrewJoinRequestsView.Item> findCrewJoinRequestItemsByCrewId(
		@Param("crewId") Long crewId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus,
		@Param("approvedStatus") CrewJoinRequestStatus approvedStatus,
		@Param("rejectedStatus") CrewJoinRequestStatus rejectedStatus,
		Pageable pageable
	);

	@Query("""
		select new com.bangpot.crew.domain.view.PendingCrewJoinRequestsView$Item(
			request.id,
			user.id,
			user.nickname
		)
		from CrewJoinRequest request, UserJpaEntity user
		where request.userId = user.id
		  and request.crewId = :crewId
		  and request.status = :pendingStatus
		  and user.withdrawnAt is null
		order by request.id asc
		""")
	Slice<PendingCrewJoinRequestsView.Item> findPendingCrewJoinRequestItemsByCrewId(
		@Param("crewId") Long crewId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus,
		Pageable pageable
	);

	@Query("""
		select
			cjr.id as joinRequestId,
			c.id as crewId,
			c.name as crewName,
			cjr.createdAt as requestedAt,
			cjr.message as message
		from CrewJoinRequest cjr, Crew c
		where cjr.crewId = c.id
		  and cjr.userId = :userId
		  and cjr.status = :pendingStatus
		  and c.status = :activeCrewStatus
		  and c.visibility = :publicVisibility
		order by cjr.createdAt desc, cjr.id desc
		""")
	Slice<MyPendingCrewRow> findMyPendingCrewsViewByUserId(
		@Param("userId") Long userId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility,
		Pageable pageable
	);

	interface MyPendingCrewRow {
		Long getJoinRequestId();
		Long getCrewId();
		String getCrewName();
		Instant getRequestedAt();
		String getMessage();
	}
}
