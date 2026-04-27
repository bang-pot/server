package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.crew.application.exception.CrewJoinRequestNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetPublicCrewCardsService;
import com.bangpot.crew.application.service.RequestCrewJoinService;
import com.bangpot.crew.application.service.UpdateCrewVisibilityService;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.application.usecase.UpdateCrewVisibilityUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewVisibilityUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-12T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private UpdateCrewVisibilityUseCase updateCrewVisibilityUseCase;
	private GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;
	private RequestCrewJoinUseCase requestCrewJoinUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		updateCrewVisibilityUseCase = new UpdateCrewVisibilityService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository
		);
		getPublicCrewCardsUseCase = new GetPublicCrewCardsService(new InMemoryCrewQueryRepository(crewRepository));
		requestCrewJoinUseCase = new RequestCrewJoinService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
	}

	@Test
	void leaderCanChangePublicCrewToPrivateAndPendingRequestsStayUntouched() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser requester = fullUser(2L, "requester-provider", "requester");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		CrewJoinRequest pending = CrewJoinRequest.createPending(crew.getId(), 99L, "join me");
		pending.assignId(10L);
		crewJoinRequestRepository.save(pending);

		UpdateCrewVisibilityUseCase.Result result = updateCrewVisibilityUseCase.handle(
			UpdateCrewVisibilityUseCase.Command.of(crew.getId(), leader.getId(), "PRIVATE")
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.visibility()).isEqualTo("PRIVATE");
		assertThat(getPublicCrewCardsUseCase.handle(GetPublicCrewCardsUseCase.Query.of(0, 20)).items()).isEmpty();
		assertThat(crewJoinRequestRepository.findPendingByCrewId(crew.getId())).hasSize(1);
		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), requester.getId(), "new join")
		)).isInstanceOf(CrewJoinRequestNotAllowedException.class);
	}

	@Test
	void leaderCanChangePrivateCrewToPublicAndDirectJoinBecomesAvailable() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser requester = fullUser(2L, "requester-provider", "requester");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "private crew", CrewVisibility.PRIVATE, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		UpdateCrewVisibilityUseCase.Result result = updateCrewVisibilityUseCase.handle(
			UpdateCrewVisibilityUseCase.Command.of(crew.getId(), leader.getId(), "PUBLIC")
		);

		assertThat(result.visibility()).isEqualTo("PUBLIC");
		assertThat(getPublicCrewCardsUseCase.handle(GetPublicCrewCardsUseCase.Query.of(0, 20)).items())
			.extracting(com.bangpot.crew.domain.view.PublicCrewCardsView.Item::crewId)
			.containsExactly(crew.getId());
		RequestCrewJoinUseCase.Result joinResult = requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), requester.getId(), "join please")
		);
		assertThat(joinResult.myStatus()).isEqualTo(CrewJoinViewStatus.PENDING);
		assertThat(crewJoinRequestRepository.findPendingByCrewId(crew.getId())).hasSize(1);
	}

	@Test
	void rejectsVisibilityUpdateForNonLeader() {
		AuthUser member = fullUser(2L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		assertThatThrownBy(() -> updateCrewVisibilityUseCase.handle(
			UpdateCrewVisibilityUseCase.Command.of(crew.getId(), member.getId(), "PRIVATE")
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsVisibilityUpdateForTempLeader() {
		AuthUser tempLeader = tempUser(3L, "temp-provider");
		authUserRepository.save(tempLeader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), tempLeader.getId()));

		assertThatThrownBy(() -> updateCrewVisibilityUseCase.handle(
			UpdateCrewVisibilityUseCase.Command.of(crew.getId(), tempLeader.getId(), "PRIVATE")
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsVisibilityUpdateForUnknownCrew() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);

		assertThatThrownBy(() -> updateCrewVisibilityUseCase.handle(
			UpdateCrewVisibilityUseCase.Command.of(999L, leader.getId(), "PRIVATE")
		)).isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void rejectsVisibilityUpdateForUnknownUser() {
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> updateCrewVisibilityUseCase.handle(
			UpdateCrewVisibilityUseCase.Command.of(crew.getId(), 999L, "PRIVATE")
		)).isInstanceOf(AccessDeniedException.class);
	}

	private AuthUser fullUser(Long id, String providerId, String nickname) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			nickname,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private AuthUser tempUser(Long id, String providerId) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.TEMP,
			null,
			null,
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {
		@Override
		public void deleteById(Long userId) {
		}


		private final Map<Long, AuthUser> usersById = new HashMap<>();

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			return usersById.values().stream()
				.filter(user -> user.getProvider() == provider && providerId.equals(user.getProviderId()))
				.findFirst();
		}

		public boolean existsByNickname(String nickname) {
			return usersById.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public AuthUser save(AuthUser user) {
			usersById.put(user.getId(), user);
			return user;
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


		private final InMemoryAuthUserRepository authUserRepository;

		private InMemoryUserRepository(InMemoryAuthUserRepository authUserRepository) {
			this.authUserRepository = authUserRepository;
		}

		@Override
		public Optional<User> findById(Long userId) {
			return authUserRepository.findById(userId)
				.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL)
				.map(this::toDomain);
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return authUserRepository.existsByNickname(nickname);
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return List.of();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			throw new UnsupportedOperationException();
		}

		private User toDomain(AuthUser authUser) {
			return User.create(authUser.getId(), authUser.getNickname());
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.Crew> findAnyById(Long crewId) {
			return findById(crewId);
		}

		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.bangpot.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.bangpot.crew.domain.view.MyCrewsView.Page.of(page, size, false)
			);
		}


		private final Map<Long, Crew> crewsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public boolean existsByName(String name) {
			return crewsById.values().stream().anyMatch(crew -> name.equals(crew.getName()));
		}

		@Override
		public Crew save(Crew crew) {
			if (crew.getId() == null) {
				crew.assignId(sequence++);
			}
			crewsById.put(crew.getId(), crew);
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return Optional.ofNullable(crewsById.get(crewId));
		}

		@Override
		public Optional<Crew> findByIdForUpdate(Long crewId) {
			return findById(crewId);
		}

		@Override
		public Optional<Crew> findByIdForShare(Long crewId) {
			return findById(crewId);
		}

		@Override
		public List<Crew> findActiveByMemberUserId(Long userId) {
			return List.of();
		}



				@Override
		public long countActiveByMemberUserId(Long userId) {
			return 0L;
		}
		@Override
		public long countPendingPublicByUserId(Long userId) {
			return 0L;
		}
		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.sorted((left, right) -> Long.compare(left.getId(), right.getId()))
				.toList();
		}
	}

	private static final class InMemoryCrewQueryRepository implements com.bangpot.crew.application.port.CrewQueryRepository {

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

		private final InMemoryCrewRepository crewRepository;

		private InMemoryCrewQueryRepository(InMemoryCrewRepository crewRepository) {
			this.crewRepository = crewRepository;
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewMemberAccessView> findCrewMemberAccessByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
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
		public com.bangpot.crew.domain.view.PublicCrewCardsView findPublicCrewCardsView(int page, int size) {
			List<com.bangpot.crew.domain.view.PublicCrewCardsView.Item> items = crewRepository.findPublicCrews().stream()
				.map(crew -> com.bangpot.crew.domain.view.PublicCrewCardsView.Item.of(
					crew.getId(),
					crew.getName(),
					crew.getDescription(),
					crew.getImageUrl()
				))
				.toList();
			int fromIndex = Math.min(page * size, items.size());
			int toIndex = Math.min(fromIndex + size, items.size());
			return com.bangpot.crew.domain.view.PublicCrewCardsView.of(
				items.subList(fromIndex, toIndex),
				com.bangpot.crew.domain.view.PublicCrewCardsView.Page.of(page, size, toIndex < items.size())
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
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew>
			findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId);
		}


		private final Map<Long, CrewMember> membersById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			membersById.put(crewMember.getId(), crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId)
				.map(member -> member.getRole() == CrewRole.LEADER)
				.orElse(false);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()))
				.findFirst();
		}
		@Override
		public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
			return findAllByCrewId(crewId).stream()
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.toList();
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
			return java.util.Optional.empty();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewJoinRequest> findPendingByIdAndUserIdForUpdate(
			Long requestId,
			Long userId
		) {
			return findPendingByIdAndUserId(requestId, userId);
		}


		private final Map<Long, CrewJoinRequest> requestsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewJoinRequest save(CrewJoinRequest crewJoinRequest) {
			if (crewJoinRequest.getId() == null) {
				crewJoinRequest.assignId(sequence++);
			}
			requestsById.put(crewJoinRequest.getId(), crewJoinRequest);
			return crewJoinRequest;
		}

		@Override
		public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
			return requestsById.values().stream().anyMatch(request ->
				crewId.equals(request.getCrewId()) &&
					userId.equals(request.getUserId()) &&
					request.getStatus() == CrewJoinRequestStatus.PENDING
			);
		}

		@Override
		public List<CrewJoinRequest> findByCrewId(Long crewId) {
			return requestsById.values().stream()
				.filter(request -> crewId.equals(request.getCrewId()))
				.toList();
		}

		@Override
		public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
			return requestsById.values().stream()
				.filter(request -> crewId.equals(request.getCrewId()) && request.getStatus() == CrewJoinRequestStatus.PENDING)
				.toList();
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
			return requestsById.values().stream()
				.filter(request -> requestId.equals(request.getId())
					&& crewId.equals(request.getCrewId())
					&& request.getStatus() == CrewJoinRequestStatus.PENDING)
				.findFirst();
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewIdForUpdate(Long requestId, Long crewId) {
			return findPendingByIdAndCrewId(requestId, crewId);
		}
	}
}


