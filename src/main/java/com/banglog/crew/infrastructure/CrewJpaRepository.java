package com.banglog.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewInviteStatus;
import com.banglog.crew.domain.CrewJoinRequestStatus;
import com.banglog.crew.domain.CrewMemberStatus;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewStatus;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.crew.domain.view.CrewHubView;
import com.banglog.crew.domain.view.CrewInviteCandidateAccessView;
import com.banglog.crew.domain.view.CrewInviteCandidatesView;
import com.banglog.crew.domain.view.CrewMemberAccessView;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.crew.domain.view.CrewPoliciesView;
import com.banglog.crew.domain.view.ExploreCrewCardsView;
import com.banglog.crew.domain.view.MeetingCreateCrewsView;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.crew.domain.view.PublicCrewPreviewView;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.user.domain.view.MyWithdrawalCheckView;

interface CrewJpaRepository extends JpaRepository<Crew, Long> {

	boolean existsByName(String name);

	java.util.Optional<Crew> findByIdAndStatus(Long id, CrewStatus status);

	@Query("""
		select new com.banglog.crew.domain.view.CrewHubView(
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
		select new com.banglog.crew.infrastructure.CrewJoinViewRow(
			c.id,
			case when c.visibility = :privateVisibility and member.id is null then null else c.name end,
			case when c.visibility = :privateVisibility and member.id is null then null else c.description end,
			c.visibility,
			case when c.visibility = :privateVisibility and member.id is null then null else c.imageUrl end,
			case
				when :userId is null then :guestStatus
				when completedUser.id is null then :completionRequiredStatus
				when member.id is not null then :memberStatus
				when exists (
					select 1
					from CrewJoinRequest joinRequest
					where joinRequest.crewId = c.id
					  and joinRequest.userId = :userId
					  and joinRequest.status = :pendingJoinRequestStatus
				) then :pendingViewStatus
				when c.visibility = :publicVisibility then :canRequestStatus
				else :privateRestrictedStatus
			end
		)
		from Crew c
		left join UserJpaEntity completedUser
		  on completedUser.id = :userId
		 and completedUser.withdrawnAt is null
		left join CrewMember member
		  on member.crewId = c.id
		 and member.userId = :userId
		 and member.status = :activeMemberStatus
		where c.id = :crewId
		  and c.status = :activeCrewStatus
		""")
	Optional<CrewJoinViewRow> findCrewJoinViewByCrewIdAndUserId(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("pendingJoinRequestStatus") CrewJoinRequestStatus pendingJoinRequestStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility,
		@Param("privateVisibility") CrewVisibility privateVisibility,
		@Param("guestStatus") String guestStatus,
		@Param("completionRequiredStatus") String completionRequiredStatus,
		@Param("memberStatus") String memberStatus,
		@Param("pendingViewStatus") String pendingViewStatus,
		@Param("canRequestStatus") String canRequestStatus,
		@Param("privateRestrictedStatus") String privateRestrictedStatus
	);

	@Query("""
		select new com.banglog.crew.domain.view.CrewMemberAccessView(
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
		select new com.banglog.crew.domain.view.CrewInviteCandidateAccessView(
			c.visibility,
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
	Optional<CrewInviteCandidateAccessView> findCrewInviteCandidateAccessByCrewIdAndUserId(
		@Param("crewId") Long crewId,
		@Param("userId") Long userId,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus
	);

	@Query("""
		select new com.banglog.crew.domain.view.CrewInviteCandidatesView$Item(
			user.id,
			user.nickname
		)
		from UserJpaEntity user
		where user.withdrawnAt is null
		  and user.id <> :leaderUserId
		  and lower(user.nickname) like lower(concat('%', :nickname, '%')) escape '\\'
		  and not exists (
			select 1
			from CrewMember member
			where member.crewId = :crewId
			  and member.userId = user.id
			  and member.status = :activeMemberStatus
		  )
		  and not exists (
			select 1
			from CrewInvite invite
			where invite.crewId = :crewId
			  and invite.targetUserId = user.id
			  and invite.status = :pendingInviteStatus
		  )
		order by lower(user.nickname) asc, user.id asc
		""")
	Slice<CrewInviteCandidatesView.Item> findCrewInviteCandidateItems(
		@Param("crewId") Long crewId,
		@Param("leaderUserId") Long leaderUserId,
		@Param("nickname") String nickname,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("pendingInviteStatus") CrewInviteStatus pendingInviteStatus,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.crew.domain.view.CrewMembersView$Item(
			user.id,
			user.nickname,
			user.profileImageUrl,
			user.bio,
			user.gender,
			(
				select count(hostMeeting.id)
				from Meeting hostMeeting
				where hostMeeting.crewId = :crewId
				  and hostMeeting.hostUserId = user.id
				  and hostMeeting.status = :completedMeetingStatus
			) + (
				select count(joinedMeeting.id)
				from MeetingParticipant participant, Meeting joinedMeeting
				where participant.meetingId = joinedMeeting.id
				  and participant.userId = user.id
				  and participant.status in :joinedParticipationStatuses
				  and joinedMeeting.crewId = :crewId
				  and joinedMeeting.status = :completedMeetingStatus
				  and joinedMeeting.hostUserId <> user.id
			),
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
	Slice<CrewMembersView.Item> findCrewMemberItemsByCrewId(
		@Param("crewId") Long crewId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole,
		@Param("completedMeetingStatus") MeetingStatus completedMeetingStatus,
		@Param("joinedParticipationStatuses") List<MeetingParticipationStatus> joinedParticipationStatuses,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.crew.domain.view.CrewPoliciesView$Item(
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
		select new com.banglog.crew.domain.view.MyCrewsView$Item(
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
		@Param("leaderRole") com.banglog.crew.domain.CrewRole leaderRole,
		Pageable pageable
	);

	@Query("""
		select count(c.id)
		from CrewMember member, Crew c
		where member.crewId = c.id
		  and member.userId = :userId
		  and member.status = :activeMemberStatus
		  and c.status = :activeCrewStatus
		""")
	long countMyCrewsViewByMemberUserId(
		@Param("userId") Long userId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus
	);

	@Query("""
		select new com.banglog.crew.domain.view.PublicCrewPreviewView$Item(
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
		select new com.banglog.crew.domain.view.ExploreCrewCardsView$Item(
			c.id,
			c.name,
			c.description,
			c.imageUrl,
			c.visibility,
			leaderUser.nickname,
			count(activeMember.id)
		)
		from Crew c
		join CrewMember leaderMember
		  on leaderMember.crewId = c.id
		 and leaderMember.role = :leaderRole
		 and leaderMember.status = :activeMemberStatus
		join UserJpaEntity leaderUser
		  on leaderUser.id = leaderMember.userId
		left join CrewMember activeMember
		  on activeMember.crewId = c.id
		 and activeMember.status = :activeMemberStatus
		where c.status = :activeCrewStatus
		  and (
			:keyword = ''
			or lower(c.name) like lower(concat('%', :keyword, '%')) escape '\\'
			or lower(leaderUser.nickname) like lower(concat('%', :keyword, '%')) escape '\\'
		  )
		group by c.id, c.name, c.description, c.imageUrl, c.visibility, c.createdAt, leaderUser.nickname
		order by c.createdAt desc, c.id desc
		""")
	Slice<ExploreCrewCardsView.Item> findExploreCrewCardItemsOrderByLatest(
		@Param("keyword") String keyword,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.crew.domain.view.ExploreCrewCardsView$Item(
			c.id,
			c.name,
			c.description,
			c.imageUrl,
			c.visibility,
			leaderUser.nickname,
			count(activeMember.id)
		)
		from Crew c
		join CrewMember leaderMember
		  on leaderMember.crewId = c.id
		 and leaderMember.role = :leaderRole
		 and leaderMember.status = :activeMemberStatus
		join UserJpaEntity leaderUser
		  on leaderUser.id = leaderMember.userId
		left join CrewMember activeMember
		  on activeMember.crewId = c.id
		 and activeMember.status = :activeMemberStatus
		where c.status = :activeCrewStatus
		  and (
			:keyword = ''
			or lower(c.name) like lower(concat('%', :keyword, '%')) escape '\\'
			or lower(leaderUser.nickname) like lower(concat('%', :keyword, '%')) escape '\\'
		  )
		group by c.id, c.name, c.description, c.imageUrl, c.visibility, c.createdAt, leaderUser.nickname
		order by c.createdAt asc, c.id asc
		""")
	Slice<ExploreCrewCardsView.Item> findExploreCrewCardItemsOrderByOldest(
		@Param("keyword") String keyword,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.crew.domain.view.ExploreCrewCardsView$Item(
			c.id,
			c.name,
			c.description,
			c.imageUrl,
			c.visibility,
			leaderUser.nickname,
			count(activeMember.id)
		)
		from Crew c
		join CrewMember leaderMember
		  on leaderMember.crewId = c.id
		 and leaderMember.role = :leaderRole
		 and leaderMember.status = :activeMemberStatus
		join UserJpaEntity leaderUser
		  on leaderUser.id = leaderMember.userId
		left join CrewMember activeMember
		  on activeMember.crewId = c.id
		 and activeMember.status = :activeMemberStatus
		where c.status = :activeCrewStatus
		  and (
			:keyword = ''
			or lower(c.name) like lower(concat('%', :keyword, '%')) escape '\\'
			or lower(leaderUser.nickname) like lower(concat('%', :keyword, '%')) escape '\\'
		  )
		group by c.id, c.name, c.description, c.imageUrl, c.visibility, c.createdAt, leaderUser.nickname
		order by count(activeMember.id) desc, c.createdAt desc, c.id desc
		""")
	Slice<ExploreCrewCardsView.Item> findExploreCrewCardItemsOrderByMemberCountDesc(
		@Param("keyword") String keyword,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole,
		Pageable pageable
	);

	@Query("""
		select new com.banglog.crew.domain.view.ExploreCrewCardsView$Item(
			c.id,
			c.name,
			c.description,
			c.imageUrl,
			c.visibility,
			leaderUser.nickname,
			count(activeMember.id)
		)
		from Crew c
		join CrewMember leaderMember
		  on leaderMember.crewId = c.id
		 and leaderMember.role = :leaderRole
		 and leaderMember.status = :activeMemberStatus
		join UserJpaEntity leaderUser
		  on leaderUser.id = leaderMember.userId
		left join CrewMember activeMember
		  on activeMember.crewId = c.id
		 and activeMember.status = :activeMemberStatus
		where c.status = :activeCrewStatus
		  and (
			:keyword = ''
			or lower(c.name) like lower(concat('%', :keyword, '%')) escape '\\'
			or lower(leaderUser.nickname) like lower(concat('%', :keyword, '%')) escape '\\'
		  )
		group by c.id, c.name, c.description, c.imageUrl, c.visibility, c.createdAt, leaderUser.nickname
		order by count(activeMember.id) asc, c.createdAt desc, c.id desc
		""")
	Slice<ExploreCrewCardsView.Item> findExploreCrewCardItemsOrderByMemberCountAsc(
		@Param("keyword") String keyword,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("leaderRole") CrewRole leaderRole,
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
		select new com.banglog.crew.domain.view.MeetingCreateCrewsView$Item(
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
		select new com.banglog.user.domain.view.MyWithdrawalCheckView$BlockingActiveCrew(
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
