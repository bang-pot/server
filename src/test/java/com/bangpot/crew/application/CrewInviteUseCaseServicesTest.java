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
import com.bangpot.crew.application.exception.CrewInviteAlreadyPendingException;
import com.bangpot.crew.application.exception.CrewInviteNotAllowedException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.CreateCrewInviteService;
import com.bangpot.crew.application.service.GetCrewInviteCandidatesService;
import com.bangpot.crew.application.usecase.CreateCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetCrewInviteCandidatesUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewInviteUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-10T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private CompletedUserAccessService completedUserAccessService;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewInviteRepository crewInviteRepository;
	private GetCrewInviteCandidatesUseCase getCrewInviteCandidatesUseCase;
	private CreateCrewInviteUseCase createCrewInviteUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewInviteRepository = new InMemoryCrewInviteRepository();
		getCrewInviteCandidatesUseCase = new GetCrewInviteCandidatesService(
			completedUserAccessService,
			userRepository,
			crewRepository,
			crewMemberRepository,
			crewInviteRepository
		);
		createCrewInviteUseCase = new CreateCrewInviteService(
			completedUserAccessService,
			userRepository,
			crewRepository,
			crewMemberRepository,
			crewInviteRepository
		);
	}

	@Test
	void returnsInvitableCandidatesForPrivateCrewLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser invited = fullUser(2L, "invited-provider", "alpha");
		AuthUser alreadyMember = fullUser(3L, "member-provider", "member");
		AuthUser alreadyPending = fullUser(4L, "pending-provider", "pending");
		AuthUser tempUser = tempUser(5L, "temp-provider");
		authUserRepository.save(leader);
		authUserRepository.save(invited);
		authUserRepository.save(alreadyMember);
		authUserRepository.save(alreadyPending);
		authUserRepository.save(tempUser);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), alreadyMember.getId()));
		crewInviteRepository.save(CrewInvite.createPending(crew.getId(), leader.getId(), alreadyPending.getId()));

		List<GetCrewInviteCandidatesUseCase.View> result = getCrewInviteCandidatesUseCase.handle(
			GetCrewInviteCandidatesUseCase.Query.of(crew.getId(), leader.getId(), null)
		);

		assertThat(result).singleElement()
			.extracting(
				GetCrewInviteCandidatesUseCase.View::userId,
				GetCrewInviteCandidatesUseCase.View::nickname
			)
			.containsExactly(invited.getId(), "alpha");
	}

	@Test
	void filtersInviteCandidatesByNickname() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		authUserRepository.save(fullUser(2L, "alpha-provider", "alpha-runner"));
		authUserRepository.save(fullUser(3L, "beta-provider", "beta-night"));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		List<GetCrewInviteCandidatesUseCase.View> result = getCrewInviteCandidatesUseCase.handle(
			GetCrewInviteCandidatesUseCase.Query.of(crew.getId(), leader.getId(), "alpha")
		);

		assertThat(result).singleElement()
			.extracting(
				GetCrewInviteCandidatesUseCase.View::userId,
				GetCrewInviteCandidatesUseCase.View::nickname
			)
			.containsExactly(2L, "alpha-runner");
	}

	@Test
	void rejectsInviteCandidatesLookupForPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("?? ?? ??", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> getCrewInviteCandidatesUseCase.handle(
			GetCrewInviteCandidatesUseCase.Query.of(crew.getId(), leader.getId(), null)
		))
			.isInstanceOf(CrewInviteNotAllowedException.class);
	}

	@Test
	void createsPendingInviteForPrivateCrewLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(target);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		CreateCrewInviteUseCase.Result result = createCrewInviteUseCase.handle(
			CreateCrewInviteUseCase.Command.of(crew.getId(), leader.getId(), target.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.targetUserId()).isEqualTo(target.getId());
		assertThat(result.status()).isEqualTo("PENDING");
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), target.getId())).isFalse();
		assertThat(crewInviteRepository.findPendingByCrewIdAndTargetUserId(crew.getId(), target.getId())).isPresent()
			.get()
			.extracting(CrewInvite::getStatus)
			.isEqualTo(CrewInviteStatus.PENDING);
	}

	@Test
	void rejectsInviteCreationWhenTargetAlreadyJoined() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(target);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), target.getId()));

		assertThatThrownBy(() -> createCrewInviteUseCase.handle(
			CreateCrewInviteUseCase.Command.of(crew.getId(), leader.getId(), target.getId())
		))
			.isInstanceOf(com.bangpot.crew.application.exception.CrewAlreadyJoinedException.class);
	}

	@Test
	void rejectsInviteCreationWhenPendingInviteAlreadyExists() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(target);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewInviteRepository.save(CrewInvite.createPending(crew.getId(), leader.getId(), target.getId()));

		assertThatThrownBy(() -> createCrewInviteUseCase.handle(
			CreateCrewInviteUseCase.Command.of(crew.getId(), leader.getId(), target.getId())
		))
			.isInstanceOf(CrewInviteAlreadyPendingException.class);
	}

	@Test
	void rejectsInviteCreationForNonLeader() {
		Crew crew = crewRepository.save(Crew.create("?? ??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser member = fullUser(1L, "member-provider", "member");
		AuthUser target = fullUser(2L, "target-provider", "runner");
		authUserRepository.save(member);
		authUserRepository.save(target);

		assertThatThrownBy(() -> createCrewInviteUseCase.handle(
			CreateCrewInviteUseCase.Command.of(crew.getId(), member.getId(), target.getId())
		))
			.isInstanceOf(AccessDeniedException.class);
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
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.toList();
		}
	}

	private static final class InMemoryCrewInviteRepository implements CrewInviteRepository {

		private final Map<Long, CrewInvite> invitesById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewInvite save(CrewInvite invite) {
			if (invite.getId() == null) {
				invite.assignId(sequence++);
			}
			invitesById.put(invite.getId(), invite);
			return invite;
		}

		@Override
		public boolean existsPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId) {
			return invitesById.values().stream()
				.anyMatch(invite ->
					crewId.equals(invite.getCrewId()) &&
					targetUserId.equals(invite.getTargetUserId()) &&
					invite.getStatus() == CrewInviteStatus.PENDING
				);
		}

		@Override
		public Optional<CrewInvite> findPendingByCrewIdAndTargetUserId(Long crewId, Long targetUserId) {
			return invitesById.values().stream()
				.filter(invite ->
					crewId.equals(invite.getCrewId()) &&
					targetUserId.equals(invite.getTargetUserId()) &&
					invite.getStatus() == CrewInviteStatus.PENDING
				)
				.findFirst();
		}

		@Override
		public Optional<CrewInvite> findPendingByIdAndTargetUserId(Long inviteId, Long targetUserId) {
			return invitesById.values().stream()
				.filter(invite ->
					inviteId.equals(invite.getId()) &&
					targetUserId.equals(invite.getTargetUserId()) &&
					invite.getStatus() == CrewInviteStatus.PENDING
				)
				.findFirst();
		}

		@Override
		public Optional<CrewInvite> findById(Long inviteId) {
			return Optional.ofNullable(invitesById.get(inviteId));
		}

		@Override
		public List<CrewInvite> findByTargetUserId(Long targetUserId) {
			return invitesById.values().stream()
				.filter(invite -> targetUserId.equals(invite.getTargetUserId()))
				.toList();
		}
	}
}


