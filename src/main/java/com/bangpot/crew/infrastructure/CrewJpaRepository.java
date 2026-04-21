package com.bangpot.crew.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;

interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	boolean existsByName(String name);

	java.util.Optional<Crew> findByIdAndStatus(Long id, CrewStatus status);

	List<Crew> findAllByStatusAndVisibilityOrderByIdAsc(CrewStatus status, CrewVisibility visibility);

	@Query("""
		select c
		from CrewMember cm, Crew c
		where cm.crewId = c.id
		  and cm.userId = :userId
		  and cm.status = :memberStatus
		  and c.status = :crewStatus
		order by c.name asc, c.id asc
		""")
	List<Crew> findActiveByMemberUserId(
		@Param("userId") Long userId,
		@Param("memberStatus") CrewMemberStatus memberStatus,
		@Param("crewStatus") CrewStatus crewStatus
	);

	@Query("""
		select count(cm)
		from CrewMember cm, Crew c
		where cm.crewId = c.id
		  and cm.userId = :userId
		  and cm.status = :memberStatus
		  and c.status = :crewStatus
		""")
	long countActiveByMemberUserId(
		@Param("userId") Long userId,
		@Param("memberStatus") CrewMemberStatus memberStatus,
		@Param("crewStatus") CrewStatus crewStatus
	);

	@Query("""
		select count(cjr)
		from CrewJoinRequest cjr, Crew c
		where cjr.userId = :userId
		  and cjr.crewId = c.id
		  and cjr.status = :requestStatus
		  and c.status = :crewStatus
		  and c.visibility = :visibility
		""")
	long countPendingPublicByUserId(
		@Param("userId") Long userId,
		@Param("requestStatus") CrewJoinRequestStatus requestStatus,
		@Param("crewStatus") CrewStatus crewStatus,
		@Param("visibility") CrewVisibility visibility
	);
}
