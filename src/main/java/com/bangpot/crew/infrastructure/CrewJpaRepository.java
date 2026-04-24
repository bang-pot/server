package com.bangpot.crew.infrastructure;

import java.util.List;

import com.bangpot.crew.domain.view.MyCrewsView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;

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
		select new com.bangpot.crew.domain.view.MyCrewsView$Item(
			c.id,
			c.name,
			c.visibility,
			leaderUser.nickname,
			c.imageUrl
		)
		from CrewMember member, Crew c, CrewMember leaderMember, UserJpaEntity leaderUser
		where member.crewId = c.id
		  and leaderMember.crewId = c.id
		  and leaderUser.id = leaderMember.userId
		  and member.userId = :userId
		  and member.status = :activeMemberStatus
		  and c.status = :activeCrewStatus
		  and leaderMember.role = :leaderRole
		  and leaderMember.status = :activeMemberStatus
		order by c.name asc, c.id asc
		""")
	Slice<MyCrewsView.Item> findMyCrewsViewByMemberUserId(
		@Param("userId") Long userId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("leaderRole") com.bangpot.crew.domain.CrewRole leaderRole,
		Pageable pageable
	);

	@Query("""
		select count(c)
		from CrewMember member, Crew c, CrewMember leaderMember, UserJpaEntity leaderUser
		where member.crewId = c.id
		  and leaderMember.crewId = c.id
		  and leaderUser.id = leaderMember.userId
		  and member.userId = :userId
		  and member.status = :activeMemberStatus
		  and c.status = :activeCrewStatus
		  and leaderMember.role = :leaderRole
		  and leaderMember.status = :activeMemberStatus
		""")
	long countMyCrewsViewByMemberUserId(
		@Param("userId") Long userId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("leaderRole") com.bangpot.crew.domain.CrewRole leaderRole
	);

	@Query("""
		select new com.bangpot.crew.domain.view.PublicCrewPreviewView$Item(
			c.id,
			c.name,
			c.imageUrl,
			(
				select count(cm.id)
				from CrewMember cm
				where cm.crewId = c.id
				  and cm.status = :activeMemberStatus
			)
		)
		from Crew c
		where c.status = :activeCrewStatus
		  and c.visibility = :publicVisibility
		order by c.id desc
		""")
	List<PublicCrewPreviewView.Item> findPublicCrewPreviewItems(
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		Pageable pageable
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

	@Query("""
		select new com.bangpot.user.domain.view.MyWithdrawalCheckView$BlockingActiveCrew(
			c.id,
			c.name
		)
		from CrewMember cm, Crew c
		where cm.crewId = c.id
		  and cm.userId = :userId
		  and cm.status = :memberStatus
		  and c.status = :crewStatus
		order by c.name asc, c.id asc
		""")
	List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(
		@Param("userId") Long userId,
		@Param("memberStatus") CrewMemberStatus memberStatus,
		@Param("crewStatus") CrewStatus crewStatus
	);
}
