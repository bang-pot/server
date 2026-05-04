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
import com.bangpot.crew.application.exception.CrewAlreadyJoinedException;
import com.bangpot.crew.application.exception.CrewJoinRequestAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewJoinRequestNotAllowedException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewJoinViewService;
import com.bangpot.crew.application.service.RequestCrewJoinService;
import com.bangpot.crew.application.service.CancelCrewJoinRequestService;
import com.bangpot.crew.application.usecase.CancelCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewJoinView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewJoinUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-08T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private GetCrewJoinViewUseCase getCrewJoinViewUseCase;
	private RequestCrewJoinUseCase requestCrewJoinUseCase;
	private CancelCrewJoinRequestUseCase cancelCrewJoinRequestUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		getCrewJoinViewUseCase = new GetCrewJoinViewService(
			new InMemoryCrewQueryRepository(
				crewRepository,
				crewMemberRepository,
				crewJoinRequestRepository,
				userRepository
			)
		);
		requestCrewJoinUseCase = new RequestCrewJoinService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		cancelCrewJoinRequestUseCase = new CancelCrewJoinRequestService(
			new CompletedUserAccessService(userRepository),
			crewJoinRequestRepository
		);
	}

	@Test
	void returnsGuestStatusForUnauthenticatedUserOnPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));

		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), null)
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.GUEST);
	}

	@Test
	void returnsCompletionRequiredStatusForTempUser() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser tempUser = tempUser(10L, "temp-user");
		authUserRepository.save(tempUser);

		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), tempUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.COMPLETION_REQUIRED);
	}

	@Test
	void returnsCanRequestStatusForFullNonMemberOnPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(11L, "full-user");
		authUserRepository.save(fullUser);

		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.CAN_REQUEST);
	}

	@Test
	void returnsPendingStatusWhenJoinRequestAlreadyExists() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(12L, "full-user");
		authUserRepository.save(fullUser);
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), fullUser.getId(), "?? ??? ???"));

		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PENDING);
	}

	@Test
	void returnsMemberStatusWhenUserAlreadyJoinedCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(13L, "full-user");
		authUserRepository.save(fullUser);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), fullUser.getId()));

		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.MEMBER);
	}

	@Test
	void returnsPrivateRestrictedStatusForPrivateCrewNonMember() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "??? ??", CrewVisibility.PRIVATE, null));
		AuthUser fullUser = fullUser(14L, "full-user");
		authUserRepository.save(fullUser);

		CrewJoinView result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PRIVATE_RESTRICTED);
		assertThat(result.name()).isNull();
		assertThat(result.description()).isNull();
		assertThat(result.imageUrl()).isNull();
	}

	@Test
	void createsPendingJoinRequestForEligibleFullUser() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(15L, "full-user");
		authUserRepository.save(fullUser);

		RequestCrewJoinUseCase.Result result = requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), "  ?? ??? ??? ")
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PENDING);
		assertThat(crewRepository.findByIdForUpdateCallCount).isEqualTo(1);
		assertThat(crewJoinRequestRepository.findByCrewIdAndUserId(crew.getId(), fullUser.getId())).get()
			.extracting(CrewJoinRequest::getMessage, CrewJoinRequest::getStatus)
			.containsExactly("?? ??? ???", CrewJoinRequestStatus.PENDING);
	}

	@Test
	void rejectsJoinRequestForPrivateCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "??? ??", CrewVisibility.PRIVATE, null));
		AuthUser fullUser = fullUser(16L, "full-user");
		authUserRepository.save(fullUser);

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), "  ?? ??? ??? ")
		))
			.isInstanceOf(CrewJoinRequestNotAllowedException.class);
	}

	@Test
	void rejectsJoinRequestWhenAlreadyJoined() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(17L, "full-user");
		authUserRepository.save(fullUser);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), fullUser.getId()));

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), "  ?? ??? ??? ")
		))
			.isInstanceOf(CrewAlreadyJoinedException.class);
	}

	@Test
	void rejectsJoinRequestWhenAlreadyPending() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(18L, "full-user");
		authUserRepository.save(fullUser);
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), fullUser.getId(), "?? ??? ???"));

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), "  ?? ??? ??? ")
		))
			.isInstanceOf(CrewJoinRequestAlreadyPendingException.class);
	}

	@Test
	void rejectsJoinRequestForTempUser() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser tempUser = tempUser(19L, "temp-user");
		authUserRepository.save(tempUser);

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), tempUser.getId(), null)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void cancelsMyPendingJoinRequestWithRowLock() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(20L, "full-user");
		authUserRepository.save(fullUser);
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), fullUser.getId(), "?? ??? ???")
		);

		CancelCrewJoinRequestUseCase.Result result = cancelCrewJoinRequestUseCase.handle(
			CancelCrewJoinRequestUseCase.Command.of(fullUser.getId(), joinRequest.getId())
		);

		assertThat(result.requestId()).isEqualTo(joinRequest.getId());
		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(crewJoinRequestRepository.findPendingByIdAndUserIdForUpdateCallCount).isEqualTo(1);
		assertThat(crewJoinRequestRepository.findById(joinRequest.getId())).get()
			.extracting(CrewJoinRequest::getStatus)
			.isEqualTo(CrewJoinRequestStatus.CANCELED);
	}

	@Test
	void rejectsUnknownCrewWhenQuerying() {
		assertThatThrownBy(() -> getCrewJoinViewUseCase.handle(GetCrewJoinViewUseCase.Query.of(999L, null)))
			.isInstanceOf(CrewNotFoundException.class);
	}

	private AuthUser fullUser(Long id, String providerId) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			"bangpot",
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

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {
		@Override
		public com.bangpot.crew.domain.view.ExploreCrewCardsView findExploreCrewCardsView(
			String keyword,
			com.bangpot.crew.domain.ExploreCrewSort sort,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
		private final InMemoryUserRepository userRepository;

		private InMemoryCrewQueryRepository(
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
		public Optional<CrewJoinView> findCrewJoinViewByCrewIdAndUserId(Long crewId, Long userId) {
			return crewRepository.findById(crewId)
				.map(crew -> {
					CrewJoinViewStatus myStatus = resolveMyStatus(crew, userId);
					boolean hidePrivateDetails = crew.getVisibility() == CrewVisibility.PRIVATE
						&& (userId == null || !crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), userId));
					return CrewJoinView.of(
						crew.getId(),
						hidePrivateDetails ? null : crew.getName(),
						hidePrivateDetails ? null : crew.getDescription(),
						crew.getVisibility(),
						hidePrivateDetails ? null : crew.getImageUrl(),
						myStatus
					);
				});
		}

		private CrewJoinViewStatus resolveMyStatus(Crew crew, Long userId) {
			if (userId == null) {
				return CrewJoinViewStatus.GUEST;
			}
			if (userRepository.findById(userId).isEmpty()) {
				return CrewJoinViewStatus.COMPLETION_REQUIRED;
			}
			if (crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), userId)) {
				return CrewJoinViewStatus.MEMBER;
			}
			if (crewJoinRequestRepository.existsPendingByCrewIdAndUserId(crew.getId(), userId)) {
				return CrewJoinViewStatus.PENDING;
			}
			if (crew.allowsDirectJoinRequest()) {
				return CrewJoinViewStatus.CAN_REQUEST;
			}
			return CrewJoinViewStatus.PRIVATE_RESTRICTED;
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
			return membersById.values().stream()
				.anyMatch(member ->
					crewId.equals(member.getCrewId()) &&
					userId.equals(member.getUserId()) &&
					member.getRole() == com.bangpot.crew.domain.CrewRole.LEADER
				);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()) && member.isActive())
				.findFirst();
		}

		@Override
		public Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
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
				.filter(member -> crewId.equals(member.getCrewId()) && member.isActive())
				.toList();
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
			CrewJoinRequest request = requestsById.get(requestId);
			if (request == null || !userId.equals(request.getUserId()) || request.getStatus() != CrewJoinRequestStatus.PENDING) {
				return Optional.empty();
			}
			return Optional.of(request);
		}


		private final Map<Long, CrewJoinRequest> requestsById = new HashMap<>();
		private long sequence = 1L;
		private int findPendingByIdAndUserIdForUpdateCallCount;

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

		Optional<CrewJoinRequest> findByCrewIdAndUserId(Long crewId, Long userId) {
			return requestsById.values().stream()
				.filter(request -> crewId.equals(request.getCrewId()) && userId.equals(request.getUserId()))
				.findFirst();
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

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndUserIdForUpdate(Long requestId, Long userId) {
			findPendingByIdAndUserIdForUpdateCallCount++;
			return findPendingByIdAndUserId(requestId, userId);
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewIdForUpdate(Long requestId, Long crewId) {
			return findPendingByIdAndCrewId(requestId, crewId);
		}

		Optional<CrewJoinRequest> findById(Long requestId) {
			return Optional.ofNullable(requestsById.get(requestId));
		}

	}
}

