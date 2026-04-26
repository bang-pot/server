package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.service.GetMeetingCreateCrewsService;
import com.bangpot.crew.application.usecase.GetMeetingCreateCrewsUseCase;
import com.bangpot.crew.domain.view.MeetingCreateCrewsView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetMeetingCreateCrewsServiceTest {

	private InMemoryCrewQueryRepository crewQueryRepository;
	private InMemoryUserRepository userRepository;
	private GetMeetingCreateCrewsUseCase useCase;

	@BeforeEach
	void setUp() {
		crewQueryRepository = new InMemoryCrewQueryRepository();
		userRepository = new InMemoryUserRepository();
		useCase = new GetMeetingCreateCrewsService(
			new CompletedUserAccessService(userRepository),
			crewQueryRepository
		);
	}

	@Test
	void returnsActiveCrewsForCompletedUser() {
		userRepository.save(User.create(1L, "alpha"));
		crewQueryRepository.saveMeetingCreateCrews(
			1L,
			MeetingCreateCrewsView.of(List.of(
				MeetingCreateCrewsView.Item.of(100L, "Alpha Crew"),
				MeetingCreateCrewsView.Item.of(200L, "Beta Crew")
			))
		);

		GetMeetingCreateCrewsUseCase.Result result = useCase.handle(GetMeetingCreateCrewsUseCase.Query.of(1L));

		assertThat(result.crews())
			.extracting(GetMeetingCreateCrewsUseCase.CrewItem::crewName)
			.containsExactly("Alpha Crew", "Beta Crew");
	}

	@Test
	void returnsEmptyArrayWhenUserHasNoActiveCrews() {
		userRepository.save(User.create(1L, "alpha"));

		GetMeetingCreateCrewsUseCase.Result result = useCase.handle(GetMeetingCreateCrewsUseCase.Query.of(1L));

		assertThat(result.crews()).isEmpty();
	}

	@Test
	void deniesIncompleteUser() {
		assertThatThrownBy(() -> useCase.handle(GetMeetingCreateCrewsUseCase.Query.of(1L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMemberAccessView>
			findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		private final java.util.Map<Long, MeetingCreateCrewsView> meetingCreateCrewsByUserId =
			new java.util.HashMap<>();

		void saveMeetingCreateCrews(Long userId, MeetingCreateCrewsView view) {
			meetingCreateCrewsByUserId.put(userId, view);
		}

		@Override
		public MeetingCreateCrewsView findMeetingCreateCrewsByMemberUserId(Long userId) {
			return meetingCreateCrewsByUserId.getOrDefault(userId, MeetingCreateCrewsView.of(List.of()));
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}
		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
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
		public List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew>
		findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}

		private final List<User> users = new ArrayList<>();

		@Override
		public Optional<User> findById(Long userId) {
			return users.stream()
				.filter(user -> user.getId().equals(userId))
				.findFirst();
		}

		@Override
		public boolean existsByNickname(String nickname) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			users.removeIf(existing -> existing.getId().equals(user.getId()));
			users.add(user);
			return user;
		}
	}
}
