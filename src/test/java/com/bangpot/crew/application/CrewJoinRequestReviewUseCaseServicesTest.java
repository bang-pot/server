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
import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestQueryRepository;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.ApproveCrewJoinRequestService;
import com.bangpot.crew.application.service.GetCrewJoinRequestsService;
import com.bangpot.crew.application.service.GetPendingCrewJoinRequestsService;
import com.bangpot.crew.application.service.RejectCrewJoinRequestService;
import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewJoinRequestManagementAccessView;
import com.bangpot.crew.domain.view.CrewJoinRequestsView;
import com.bangpot.crew.domain.view.PendingCrewJoinRequestsView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

class CrewJoinRequestReviewUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-09T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private GetCrewJoinRequestsUseCase getCrewJoinRequestsUseCase;
	private GetPendingCrewJoinRequestsUseCase getPendingCrewJoinRequestsUseCase;
	private ApproveCrewJoinRequestUseCase approveCrewJoinRequestUseCase;
	private RejectCrewJoinRequestUseCase rejectCrewJoinRequestUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		getCrewJoinRequestsUseCase = new GetCrewJoinRequestsService(
			new InMemoryCrewJoinRequestQueryRepository(crewRepository, crewMemberRepository, crewJoinRequestRepository,
				userRepository)
		);
		getPendingCrewJoinRequestsUseCase = new GetPendingCrewJoinRequestsService(
			new InMemoryCrewJoinRequestQueryRepository(crewRepository, crewMemberRepository, crewJoinRequestRepository,
				userRepository)
		);
		approveCrewJoinRequestUseCase = new ApproveCrewJoinRequestService(
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		rejectCrewJoinRequestUseCase = new RejectCrewJoinRequestService(
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
	}

	@Test
	void returnsAllJoinRequestsForLeaderManagementView() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser pendingUser = fullUser(2L, "pending-provider", "runner");
		AuthUser rejectedUser = fullUser(3L, "rejected-provider", "guest");
		authUserRepository.save(leader);
		authUserRepository.save(pendingUser);
		authUserRepository.save(rejectedUser);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), pendingUser.getId(), "?? ?? ???"));
		CrewJoinRequest rejectedRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), rejectedUser.getId(), "???? ???")
		);
		rejectedRequest.reject();
		crewJoinRequestRepository.save(rejectedRequest);

		CrewJoinRequestsView result = getCrewJoinRequestsUseCase.handle(
			GetCrewJoinRequestsUseCase.Query.of(crew.getId(), leader.getId(), 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.items().get(0))
			.extracting(
				CrewJoinRequestsView.Item::requestId,
				CrewJoinRequestsView.Item::userId,
				CrewJoinRequestsView.Item::nickname,
				CrewJoinRequestsView.Item::message,
				CrewJoinRequestsView.Item::status
			)
			.containsExactly(1L, 2L, "runner", "?? ?? ???", "PENDING");
		assertThat(result.items().get(1))
			.extracting(
				CrewJoinRequestsView.Item::requestId,
				CrewJoinRequestsView.Item::userId,
				CrewJoinRequestsView.Item::nickname,
				CrewJoinRequestsView.Item::message,
				CrewJoinRequestsView.Item::status
			)
			.containsExactly(2L, 3L, "guest", "???? ???", "REJECTED");
		assertThat(result.page()).isEqualTo(CrewJoinRequestsView.Page.of(0, 20, false));
	}

	@Test
	void returnsPendingJoinRequestsForLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser requester = fullUser(2L, "requester-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), requester.getId(), "?? ??"));

		PendingCrewJoinRequestsView result = getPendingCrewJoinRequestsUseCase.handle(
			GetPendingCrewJoinRequestsUseCase.Query.of(crew.getId(), leader.getId(), 0, 20)
		);

		assertThat(result.items()).singleElement()
			.extracting(
				PendingCrewJoinRequestsView.Item::requestId,
				PendingCrewJoinRequestsView.Item::userId,
				PendingCrewJoinRequestsView.Item::nickname
			)
			.containsExactly(1L, 2L, "runner");
		assertThat(result.page()).isEqualTo(PendingCrewJoinRequestsView.Page.of(0, 20, false));
	}

	@Test
	void rejectsPendingJoinRequestListingForNonLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser member = fullUser(3L, "member-provider", "member");
		authUserRepository.save(member);

		assertThatThrownBy(() -> getPendingCrewJoinRequestsUseCase.handle(
			GetPendingCrewJoinRequestsUseCase.Query.of(crew.getId(), member.getId(), 0, 20)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void approvesPendingJoinRequestAndCreatesMember() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(10L, "leader-provider", "leader");
		AuthUser requester = fullUser(11L, "requester-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), "?? ??")
		);

		ApproveCrewJoinRequestUseCase.Result result = approveCrewJoinRequestUseCase.handle(
			ApproveCrewJoinRequestUseCase.Command.of(crew.getId(), joinRequest.getId(), leader.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.requestId()).isEqualTo(joinRequest.getId());
		assertThat(result.userId()).isEqualTo(requester.getId());
		assertThat(result.role()).isEqualTo(CrewRole.MEMBER);
		assertThat(crewRepository.findByIdForUpdateCallCount).isEqualTo(1);
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), requester.getId())).isTrue();
		assertThat(crewJoinRequestRepository.findPendingByIdAndCrewId(joinRequest.getId(), crew.getId())).isEmpty();
		assertThat(crewJoinRequestRepository.findById(joinRequest.getId())).get()
			.extracting(CrewJoinRequest::getStatus)
			.isEqualTo(CrewJoinRequestStatus.APPROVED);
	}

	@Test
	void rejectsPendingJoinRequestForLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(20L, "leader-provider", "leader");
		AuthUser requester = fullUser(21L, "requester-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), "?? ??")
		);

		RejectCrewJoinRequestUseCase.Result result = rejectCrewJoinRequestUseCase.handle(
			RejectCrewJoinRequestUseCase.Command.of(crew.getId(), joinRequest.getId(), leader.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.requestId()).isEqualTo(joinRequest.getId());
		assertThat(crewJoinRequestRepository.findPendingByIdAndCrewId(joinRequest.getId(), crew.getId())).isEmpty();
		assertThat(crewJoinRequestRepository.findById(joinRequest.getId())).get()
			.extracting(CrewJoinRequest::getStatus)
			.isEqualTo(CrewJoinRequestStatus.REJECTED);
	}

	@Test
	void rejectsApproveForNonLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser nonLeader = fullUser(30L, "member-provider", "member");
		AuthUser requester = fullUser(31L, "requester-provider", "runner");
		authUserRepository.save(nonLeader);
		authUserRepository.save(requester);
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), "?? ??")
		);

		assertThatThrownBy(() -> approveCrewJoinRequestUseCase.handle(
			ApproveCrewJoinRequestUseCase.Command.of(crew.getId(), joinRequest.getId(), nonLeader.getId())
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsUnknownPendingJoinRequest() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(40L, "leader-provider", "leader");
		authUserRepository.save(leader);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> rejectCrewJoinRequestUseCase.handle(
			RejectCrewJoinRequestUseCase.Command.of(crew.getId(), 999L, leader.getId())
		))
			.isInstanceOf(CrewJoinRequestNotFoundException.class);
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
		private final Map<Long, User> usersById = new HashMap<>();

		private InMemoryUserRepository(InMemoryAuthUserRepository authUserRepository) {
			this.authUserRepository = authUserRepository;
			authUserRepository.usersById.values().stream()
				.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL && authUser.getNickname() != null)
				.map(authUser -> User.create(authUser.getId(), authUser.getNickname()))
				.forEach(user -> usersById.put(user.getId(), user));
		}

		@Override
		public Optional<User> findById(Long userId) {
			User user = usersById.get(userId);
			if (user != null) {
				return Optional.of(user);
			}
			return authUserRepository.findById(userId)
				.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL && authUser.getNickname() != null)
				.map(authUser -> {
					User loaded = User.create(authUser.getId(), authUser.getNickname());
					usersById.put(loaded.getId(), loaded);
					return loaded;
				});
		}

		public boolean existsByNickname(String nickname) {
			return usersById.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			String normalizedKeyword = nickname == null ? null : nickname.trim().toLowerCase();
			if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
				normalizedKeyword = null;
			}
			authUserRepository.usersById.values().stream()
				.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL && authUser.getNickname() != null)
				.map(authUser -> User.create(authUser.getId(), authUser.getNickname()))
				.forEach(user -> usersById.putIfAbsent(user.getId(), user));
			final String keyword = normalizedKeyword;
			return usersById.values().stream()
				.filter(user -> keyword == null || user.getNickname().toLowerCase().contains(keyword))
				.sorted((left, right) -> Long.compare(left.getId(), right.getId()))
				.toList();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			usersById.put(user.getId(), user);
			return user;
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
		private int findByIdForUpdateCallCount;

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
			findByIdForUpdateCallCount++;
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
				.toList();
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
			return membersById.values().stream()
				.anyMatch(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()));
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.anyMatch(member ->
					crewId.equals(member.getCrewId()) &&
					userId.equals(member.getUserId()) &&
					member.getRole() == CrewRole.LEADER
				);
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

	private static final class InMemoryCrewJoinRequestQueryRepository implements CrewJoinRequestQueryRepository {

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
		private final InMemoryUserRepository userRepository;

		private InMemoryCrewJoinRequestQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository,
			InMemoryCrewJoinRequestRepository crewJoinRequestRepository,
			InMemoryUserRepository userRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
			this.crewJoinRequestRepository = crewJoinRequestRepository;
			this.userRepository = userRepository;
		}

		@Override
		public Optional<CrewJoinRequestManagementAccessView> findManagementAccessByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			return crewRepository.findById(crewId)
				.map(crew -> CrewJoinRequestManagementAccessView.of(
					crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
						.map(CrewMember::getRole)
						.orElse(null)
				));
		}

		@Override
		public CrewJoinRequestsView findCrewJoinRequestsViewByCrewId(Long crewId, int page, int size) {
			List<CrewJoinRequestsView.Item> items = crewJoinRequestRepository.findByCrewId(crewId).stream()
				.flatMap(request -> userRepository.findById(request.getUserId()).stream()
					.map(user -> CrewJoinRequestsView.Item.of(
						request.getId(),
						request.getUserId(),
						user.getNickname(),
						request.getMessage(),
						request.getStatus().name()
					)))
				.toList();
			int fromIndex = Math.min(page * size, items.size());
			int toIndex = Math.min(fromIndex + size, items.size());
			return CrewJoinRequestsView.of(
				items.subList(fromIndex, toIndex),
				CrewJoinRequestsView.Page.of(page, size, toIndex < items.size())
			);
		}

		@Override
		public PendingCrewJoinRequestsView findPendingCrewJoinRequestsViewByCrewId(Long crewId, int page, int size) {
			List<PendingCrewJoinRequestsView.Item> items = crewJoinRequestRepository.findPendingByCrewId(crewId).stream()
				.flatMap(request -> userRepository.findById(request.getUserId()).stream()
					.map(user -> PendingCrewJoinRequestsView.Item.of(
						request.getId(),
						request.getUserId(),
						user.getNickname()
					)))
				.toList();
			int fromIndex = Math.min(page * size, items.size());
			int toIndex = Math.min(fromIndex + size, items.size());
			return PendingCrewJoinRequestsView.of(
				items.subList(fromIndex, toIndex),
				PendingCrewJoinRequestsView.Page.of(page, size, toIndex < items.size())
			);
		}

		@Override
		public com.bangpot.crew.domain.view.MyPendingCrewsView findMyPendingCrewsViewByUserId(
			Long userId,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
			return java.util.Optional.empty();
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
			return requestsById.values().stream()
				.anyMatch(request ->
					crewId.equals(request.getCrewId()) &&
					userId.equals(request.getUserId()) &&
					request.getStatus() == CrewJoinRequestStatus.PENDING
				);
		}

		@Override
		public List<CrewJoinRequest> findByCrewId(Long crewId) {
			return requestsById.values().stream()
				.filter(request -> crewId.equals(request.getCrewId()))
				.sorted((left, right) -> Long.compare(left.getId(), right.getId()))
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
			CrewJoinRequest request = requestsById.get(requestId);
			if (request == null || !crewId.equals(request.getCrewId()) || request.getStatus() != CrewJoinRequestStatus.PENDING) {
				return Optional.empty();
			}
			return Optional.of(request);
		}

		Optional<CrewJoinRequest> findById(Long requestId) {
			return Optional.ofNullable(requestsById.get(requestId));
		}
	}
}
