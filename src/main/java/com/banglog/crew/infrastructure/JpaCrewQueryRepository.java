package com.banglog.crew.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.domain.CrewInviteStatus;
import com.banglog.crew.domain.CrewJoinRequestStatus;
import com.banglog.crew.domain.CrewJoinViewStatus;
import com.banglog.crew.domain.CrewMemberStatus;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewStatus;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.crew.domain.ExploreCrewSort;
import com.banglog.crew.domain.view.CrewHubView;
import com.banglog.crew.domain.view.CrewInviteCandidateAccessView;
import com.banglog.crew.domain.view.CrewInviteCandidatesView;
import com.banglog.crew.domain.view.CrewJoinView;
import com.banglog.crew.domain.view.CrewMemberAccessView;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.crew.domain.view.CrewPoliciesView;
import com.banglog.crew.domain.view.ExploreCrewCardsView;
import com.banglog.crew.domain.view.MeetingCreateCrewsView;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.crew.domain.view.PublicCrewPreviewView;
import com.banglog.user.domain.view.MyWithdrawalCheckView;

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
			CrewStatus.ACTIVE
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
	public ExploreCrewCardsView findExploreCrewCardsView(String keyword, ExploreCrewSort sort, int page, int size) {
		String escapedKeyword = escapeLikeKeyword(keyword);
		Slice<ExploreCrewCardsView.Item> slice = findExploreCrewCardItems(escapedKeyword, sort, page, size);
		return ExploreCrewCardsView.of(
			slice.getContent(),
			ExploreCrewCardsView.Page.of(page, size, slice.hasNext())
		);
	}

	private Slice<ExploreCrewCardsView.Item> findExploreCrewCardItems(
		String keyword,
		ExploreCrewSort sort,
		int page,
		int size
	) {
		ExploreCrewSort normalizedSort = sort == null ? ExploreCrewSort.LATEST : sort;
		PageRequest pageRequest = PageRequest.of(page, size);
		return switch (normalizedSort) {
			case LATEST -> crewJpaRepository.findExploreCrewCardItemsOrderByLatest(
				keyword,
				CrewStatus.ACTIVE,
				CrewMemberStatus.ACTIVE,
				CrewRole.LEADER,
				pageRequest
			);
			case OLDEST -> crewJpaRepository.findExploreCrewCardItemsOrderByOldest(
				keyword,
				CrewStatus.ACTIVE,
				CrewMemberStatus.ACTIVE,
				CrewRole.LEADER,
				pageRequest
			);
			case MEMBER_COUNT_DESC -> crewJpaRepository.findExploreCrewCardItemsOrderByMemberCountDesc(
				keyword,
				CrewStatus.ACTIVE,
				CrewMemberStatus.ACTIVE,
				CrewRole.LEADER,
				pageRequest
			);
			case MEMBER_COUNT_ASC -> crewJpaRepository.findExploreCrewCardItemsOrderByMemberCountAsc(
				keyword,
				CrewStatus.ACTIVE,
				CrewMemberStatus.ACTIVE,
				CrewRole.LEADER,
				pageRequest
			);
		};
	}

	private String escapeLikeKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}
		return keyword
			.replace("\\", "\\\\")
			.replace("%", "\\%")
			.replace("_", "\\_");
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
