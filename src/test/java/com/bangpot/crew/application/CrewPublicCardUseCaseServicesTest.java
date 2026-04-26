package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.service.GetPublicCrewCardsService;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.domain.view.CrewHubView;
import com.bangpot.crew.domain.view.CrewMemberAccessView;
import com.bangpot.crew.domain.view.CrewMembersView;
import com.bangpot.crew.domain.view.CrewPoliciesView;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.crew.domain.view.MyCrewsView;
import com.bangpot.crew.domain.view.PublicCrewCardsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.user.domain.view.MyWithdrawalCheckView;

class CrewPublicCardUseCaseServicesTest {

	private InMemoryCrewQueryRepository crewQueryRepository;
	private GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;

	@BeforeEach
	void setUp() {
		crewQueryRepository = new InMemoryCrewQueryRepository();
		getPublicCrewCardsUseCase = new GetPublicCrewCardsService(crewQueryRepository);
	}

	@Test
	void returnsPublicCrewCardsView() {
		crewQueryRepository.save(PublicCrewCardsView.Item.of(1L, "Crew Alpha", "public crew", null));
		crewQueryRepository.save(PublicCrewCardsView.Item.of(
			2L,
			"Crew Beta",
			"night runners",
			"https://image.example/beta.png"
		));

		PublicCrewCardsView result = getPublicCrewCardsUseCase.handle(GetPublicCrewCardsUseCase.Query.of(0, 20));

		assertThat(result.items())
			.extracting(PublicCrewCardsView.Item::name)
			.containsExactly("Crew Alpha", "Crew Beta");
		assertThat(result.items().get(1).imageUrl()).isEqualTo("https://image.example/beta.png");
		assertThat(result.page().page()).isZero();
		assertThat(result.page().size()).isEqualTo(20);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void returnsPublicCrewCardsSlice() {
		for (long index = 1L; index <= 25L; index++) {
			crewQueryRepository.save(PublicCrewCardsView.Item.of(index, "Crew " + index, "desc", null));
		}

		PublicCrewCardsView result = getPublicCrewCardsUseCase.handle(GetPublicCrewCardsUseCase.Query.of(1, 10));

		assertThat(result.items()).hasSize(10);
		assertThat(result.items())
			.extracting(PublicCrewCardsView.Item::crewId)
			.containsExactlyElementsOf(java.util.stream.LongStream.rangeClosed(11L, 20L).boxed().toList());
		assertThat(result.page().page()).isEqualTo(1);
		assertThat(result.page().size()).isEqualTo(10);
		assertThat(result.page().hasNext()).isTrue();
	}

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewInviteCandidateAccessView>
			findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.CrewInviteCandidatesView findCrewInviteCandidatesView(
			Long crewId,
			Long leaderUserId,
			String nickname,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		private final List<PublicCrewCardsView.Item> items = new ArrayList<>();

		private void save(PublicCrewCardsView.Item item) {
			items.add(item);
		}

		@Override
		public Optional<CrewHubView> findCrewHubViewByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
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
		public PublicCrewCardsView findPublicCrewCardsView(int page, int size) {
			int fromIndex = Math.min(page * size, items.size());
			int toIndex = Math.min(fromIndex + size, items.size());
			return PublicCrewCardsView.of(
				items.subList(fromIndex, toIndex),
				PublicCrewCardsView.Page.of(page, size, toIndex < items.size())
			);
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
