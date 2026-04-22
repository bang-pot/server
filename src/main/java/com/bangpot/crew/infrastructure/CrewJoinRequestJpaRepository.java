package com.bangpot.crew.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;

interface CrewJoinRequestJpaRepository extends JpaRepository<CrewJoinRequest, Long> {

	boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, CrewJoinRequestStatus status);

	List<CrewJoinRequest> findAllByCrewIdOrderByIdAsc(Long crewId);

	List<CrewJoinRequest> findAllByCrewIdAndStatusOrderByIdAsc(Long crewId, CrewJoinRequestStatus status);

	Optional<CrewJoinRequest> findByIdAndCrewIdAndStatus(Long id, Long crewId, CrewJoinRequestStatus status);

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
