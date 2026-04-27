package com.bangpot.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewHubView;
import com.bangpot.crew.domain.view.CrewInviteCandidateAccessView;
import com.bangpot.crew.domain.view.CrewInviteCandidatesView;
import com.bangpot.crew.domain.view.CrewJoinView;
import com.bangpot.crew.domain.view.CrewMemberAccessView;
import com.bangpot.crew.domain.view.CrewMembersView;
import com.bangpot.crew.domain.view.CrewPoliciesView;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewCardsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaCrewQueryRepository implements CrewQueryRepository {

	private final CrewJpaRepository crewJpaRepository;

	@Override
	public Optional<CrewHubView> findCrewHubViewByCrewIdAndUserId(Long crewId, Long userId) {
		return crewJpaRepository.findCrewHubViewByCrewIdAndUserId(
			crewId,
			userId,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE,
			CrewRole.LEADER,
			CrewJoinRequestStatus.PENDING
		);
	}

	@Override
	public Optional<CrewJoinView> findCrewJoinViewByCrewIdAndUserId(Long crewId, Long userId) {
		return crewJpaRepository.findCrewJoinViewByCrewIdAndUserId(
			crewId,
			userId,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE,
			CrewJoinRequestStatus.PENDING,
			CrewVisibility.PUBLIC,
			CrewVisibility.PRIVATE,
			CrewJoinViewStatus.GUEST,
			CrewJoinViewStatus.COMPLETION_REQUIRED,
			CrewJoinViewStatus.MEMBER,
			CrewJoinViewStatus.PENDING,
			CrewJoinViewStatus.CAN_REQUEST,
			CrewJoinViewStatus.PRIVATE_RESTRICTED
		);
	}

	@Override
	public Optional<CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
		return crewJpaRepository.findCrewMemberAccessByCrewIdAndUserId(
			crewId,
			userId,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE
		);
	}

	@Override
	public Optional<CrewInviteCandidateAccessView> findCrewInviteCandidateAccessByCrewIdAndUserId(
		Long crewId,
		Long userId
	) {
		return crewJpaRepository.findCrewInviteCandidateAccessByCrewIdAndUserId(
			crewId,
			userId,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE
		);
	}

	@Override
	public CrewInviteCandidatesView findCrewInviteCandidatesView(
		Long crewId,
		Long leaderUserId,
		String nickname,
		int page,
		int size
	) {
		Slice<CrewInviteCandidatesView.Item> slice = crewJpaRepository.findCrewInviteCandidateItems(
			crewId,
			leaderUserId,
			nickname,
			CrewMemberStatus.ACTIVE,
			CrewInviteStatus.PENDING,
			PageRequest.of(page, size)
		);
		return CrewInviteCandidatesView.of(
			slice.getContent(),
			CrewInviteCandidatesView.Page.of(page, size, slice.hasNext())
		);
	}

	@Override
	public Optional<CrewMembersView> findCrewMembersViewByCrewIdAndUserId(Long crewId, Long userId) {
		return findCrewMemberAccessByCrewIdAndUserId(crewId, userId).map(access -> CrewMembersView.of(
			access.myRole(),
			findCrewMemberItemsIfMember(crewId, access)
		));
	}

	private List<CrewMembersView.Item> findCrewMemberItemsIfMember(Long crewId, CrewMemberAccessView access) {
		if (access.myRole() == null) {
			return List.of();
		}
		return crewJpaRepository.findCrewMemberItemsByCrewId(
			crewId,
			CrewMemberStatus.ACTIVE,
			CrewRole.LEADER
		);
	}

	@Override
	public Optional<CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(Long crewId, Long userId) {
		return findCrewMemberAccessByCrewIdAndUserId(crewId, userId).map(access -> CrewPoliciesView.of(
			access.myRole(),
			findCrewPolicyItemsIfMember(crewId, access)
		));
	}

	private List<CrewPoliciesView.Item> findCrewPolicyItemsIfMember(Long crewId, CrewMemberAccessView access) {
		if (access.myRole() == null) {
			return List.of();
		}
		return crewJpaRepository.findCrewPolicyItemsByCrewId(crewId);
	}

	@Override
	public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
		org.springframework.data.domain.Slice<MyCrewsView.Item> slice = crewJpaRepository.findMyCrewsViewByMemberUserId(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE,
			CrewRole.LEADER,
			PageRequest.of(page, size)
		);
		return MyCrewsView.of(slice.getContent(), MyCrewsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public long countMyCrewsViewByMemberUserId(Long userId) {
		return crewJpaRepository.countMyCrewsViewByMemberUserId(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE,
			CrewRole.LEADER
		);
	}

	@Override
	public PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
		return PublicCrewPreviewView.of(crewJpaRepository.findPublicCrewPreviewItems(
			CrewStatus.ACTIVE,
			CrewVisibility.PUBLIC,
			CrewMemberStatus.ACTIVE,
			PageRequest.of(0, limit)
		));
	}

	@Override
	public PublicCrewCardsView findPublicCrewCardsView(int page, int size) {
		Slice<PublicCrewCardsView.Item> slice = crewJpaRepository.findPublicCrewCardItems(
			CrewStatus.ACTIVE,
			CrewVisibility.PUBLIC,
			PageRequest.of(page, size)
		);
		return PublicCrewCardsView.of(
			slice.getContent(),
			PublicCrewCardsView.Page.of(page, size, slice.hasNext())
		);
	}

	@Override
	public long countActiveByMemberUserId(Long userId) {
		return crewJpaRepository.countActiveByMemberUserId(userId, CrewMemberStatus.ACTIVE, CrewStatus.ACTIVE);
	}

	@Override
	public long countPendingPublicByUserId(Long userId) {
		return crewJpaRepository.countPendingPublicByUserId(
			userId,
			CrewJoinRequestStatus.PENDING,
			CrewStatus.ACTIVE,
			CrewVisibility.PUBLIC
		);
	}

	@Override
	public MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
		return MeetingCreateCrewsView.of(crewJpaRepository.findMeetingCreateCrewItemsByMemberUserId(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE
		));
	}

	@Override
	public List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
		return crewJpaRepository.findWithdrawalBlockingActiveCrewsByMemberUserId(
			userId,
			CrewMemberStatus.ACTIVE,
			CrewStatus.ACTIVE
		);
	}
}
