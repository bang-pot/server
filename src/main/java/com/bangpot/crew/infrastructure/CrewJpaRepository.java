package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewHubView;
import com.bangpot.crew.domain.view.CrewMemberAccessView;
import com.bangpot.crew.domain.view.CrewMembersView;
import com.bangpot.crew.domain.view.CrewPoliciesView;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewCardsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	boolean existsByName(String name);

	java.util.Optional<Crew> findByIdAndStatus(Long id, CrewStatus status);

	@Query("""
		select new com.bangpot.crew.domain.view.CrewHubView(
			c.id,
			c.name,
			c.description,
			c.visibility,
			c.imageUrl,
			member.role,
			false,
			case
				when member.role = :leaderRole then (
					select count(joinRequest.id)
					from CrewJoinRequest joinRequest
					where joinRequest.crewId = c.id
					  and joinRequest.status = :pendingJoinRequestStatus
				)
				else null
			end
		)
		from Crew c
		left join CrewMember member
		  on member.crewId = c.id
		 and member.userId = :userId
		 and member.status = :activeMemberStatus
		where c.id = :crewId
		  and c.status = :activeCrewStatus
		""")
	Optional<CrewHubView> findCrewHubViewByCrewIdAndUserId(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole,
		@Param("pendingJoinRequestStatus") CrewJoinRequestStatus pendingJoinRequestStatus
	);

	@Query("""
		select new com.bangpot.crew.domain.view.CrewMemberAccessView(
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
	Optional<CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus
	);

	@Query("""
		select new com.bangpot.crew.domain.view.CrewMembersView$Item(
			user.id,
			user.nickname,
			user.profileImageUrl,
			user.bio,
			user.gender,
			0,
			member.role,
			member.createdAt
		)
		from CrewMember member, UserJpaEntity user
		where member.userId = user.id
		  and member.crewId = :crewId
		  and member.status = :activeMemberStatus
		  and user.withdrawnAt is null
		order by
		  case when member.role = :leaderRole then 0 else 1 end asc,
		  member.createdAt desc
		""")
	List<CrewMembersView.Item> findCrewMemberItemsByCrewId(
		@Param("crewId") Long crewId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole
	);

	@Query("""
		select new com.bangpot.crew.domain.view.CrewPoliciesView$Item(
			policy.id,
			policy.title,
			policy.content
		)
		from CrewPolicy policy
		where policy.crewId = :crewId
		order by policy.createdAt asc, policy.id asc
		""")
	List<CrewPoliciesView.Item> findCrewPolicyItemsByCrewId(@Param("crewId") Long crewId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select c
		from Crew c
		where c.id = :id
		  and c.status = :status
		""")
	java.util.Optional<Crew> findByIdAndStatusForUpdate(
		@Param("id") Long id,
		@Param("status") CrewStatus status
	);

	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("""
		select c
		from Crew c
		where c.id = :id
		  and c.status = :status
		""")
	java.util.Optional<Crew> findByIdAndStatusForShare(
		@Param("id") Long id,
		@Param("status") CrewStatus status
	);

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
		select new com.bangpot.crew.domain.view.PublicCrewCardsView$Item(
			c.id,
			c.name,
			c.description,
			c.imageUrl
		)
		from Crew c
		where c.status = :activeCrewStatus
		  and c.visibility = :publicVisibility
		order by c.id asc
		""")
	Slice<PublicCrewCardsView.Item> findPublicCrewCardItems(
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility,
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
		select new com.bangpot.crew.domain.view.MeetingCreateCrewsView$Item(
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
	List<MeetingCreateCrewsView.Item> findMeetingCreateCrewItemsByMemberUserId(
		@Param("userId") Long userId,
		@Param("memberStatus") CrewMemberStatus memberStatus,
		@Param("crewStatus") CrewStatus crewStatus
	);

	@Query("""
		select new com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew(
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
