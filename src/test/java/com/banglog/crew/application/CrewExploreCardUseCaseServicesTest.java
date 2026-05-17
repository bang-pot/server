package com.banglog.crew.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.crew.application.service.GetExploreCrewCardsService;
import com.banglog.crew.application.usecase.GetExploreCrewCardsUseCase;
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

class CrewExploreCardUseCaseServicesTest {

	private RecordingCrewQueryRepository crewQueryRepository;
	private GetExploreCrewCardsUseCase getExploreCrewCardsUseCase;

	@BeforeEach
	void setUp() {
		crewQueryRepository = new RecordingCrewQueryRepository();
		getExploreCrewCardsUseCase = new GetExploreCrewCardsService(crewQueryRepository);
	}

	@Test
	void trimsKeywordAndTreatsBlankKeywordAsDefaultExploreList() {
		getExploreCrewCardsUseCase.handle(GetExploreCrewCardsUseCase.Query.of(
			0,
			20,
			"   ",
			ExploreCrewSort.LATEST
		));

		assertThat(crewQueryRepository.keyword).isNull();
	}

	@Test
	void clampsPageSizeForExploreList() {
		getExploreCrewCardsUseCase.handle(GetExploreCrewCardsUseCase.Query.of(
			-1,
			100,
			" Alpha ",
			ExploreCrewSort.MEMBER_COUNT_DESC
		));

		assertThat(crewQueryRepository.page).isZero();
		assertThat(crewQueryRepository.size).isEqualTo(50);
		assertThat(crewQueryRepository.keyword).isEqualTo("Alpha");
		assertThat(crewQueryRepository.sort).isEqualTo(ExploreCrewSort.MEMBER_COUNT_DESC);
	}

	private static final class RecordingCrewQueryRepository implements CrewQueryRepository {

		private int page;
		private int size;
		private String keyword;
		private ExploreCrewSort sort;

		@Override
		public ExploreCrewCardsView findExploreCrewCardsView(
			String keyword,
			ExploreCrewSort sort,
			int page,
			int size
		) {
			this.keyword = keyword;
			this.sort = sort;
			this.page = page;
			this.size = size;
			return ExploreCrewCardsView.of(List.of(), ExploreCrewCardsView.Page.of(page, size, false));
		}

		@Override
		public Optional<CrewHubView> findCrewHubViewByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewJoinView> findCrewJoinViewByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewInviteCandidateAccessView> findCrewInviteCandidateAccessByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public CrewInviteCandidatesView findCrewInviteCandidatesView(
			Long crewId,
			Long leaderUserId,
			String nickname,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewMembersView> findCrewMembersViewByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(
			Long userId
		) {
			throw new UnsupportedOperationException();
		}
	}
}
