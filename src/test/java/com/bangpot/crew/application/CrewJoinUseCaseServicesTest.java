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
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewJoinViewService;
import com.bangpot.crew.application.service.RequestCrewJoinService;
import com.bangpot.crew.application.usecase.GetCrewJoinViewUseCase;
import com.bangpot.crew.application.usecase.RequestCrewJoinUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
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

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		getCrewJoinViewUseCase = new GetCrewJoinViewService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		requestCrewJoinUseCase = new RequestCrewJoinService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
	}

	@Test
	void returnsGuestStatusForUnauthenticatedUserOnPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
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

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), tempUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.COMPLETION_REQUIRED);
	}

	@Test
	void returnsCanRequestStatusForFullNonMemberOnPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "??? ??", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(11L, "full-user");
		authUserRepository.save(fullUser);

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
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

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
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

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.MEMBER);
	}

	@Test
	void returnsPrivateRestrictedStatusForPrivateCrewNonMember() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "??? ??", CrewVisibility.PRIVATE, null));
		AuthUser fullUser = fullUser(14L, "full-user");
		authUserRepository.save(fullUser);

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PRIVATE_RESTRICTED);
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

		@Override
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
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.isActive())
				.toList();
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

	}
}

