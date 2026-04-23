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
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.AcceptCrewInviteService;
import com.bangpot.crew.application.service.GetMyCrewInvitesService;
import com.bangpot.crew.application.service.RejectCrewInviteService;
import com.bangpot.crew.application.usecase.AcceptCrewInviteUseCase;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.crew.application.usecase.RejectCrewInviteUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.crew.domain.CrewInviteStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewInviteConsumerUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-11T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private CompletedUserAccessService completedUserAccessService;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewInviteRepository crewInviteRepository;
	private GetMyCrewInvitesUseCase getMyCrewInvitesUseCase;
	private AcceptCrewInviteUseCase acceptCrewInviteUseCase;
	private RejectCrewInviteUseCase rejectCrewInviteUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewInviteRepository = new InMemoryCrewInviteRepository();
		getMyCrewInvitesUseCase = new GetMyCrewInvitesService(
			completedUserAccessService,
			userRepository,
			crewRepository,
			crewInviteRepository
		);
		acceptCrewInviteUseCase = new AcceptCrewInviteService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			crewInviteRepository
		);
		rejectCrewInviteUseCase = new RejectCrewInviteService(
			completedUserAccessService,
			crewInviteRepository
		);
	}

	@Test
	void returnsMyCrewInviteHistory() {
		Crew crew = crewRepository.save(Crew.create("??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser inviter = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "target");
		authUserRepository.save(inviter);
		authUserRepository.save(target);
		crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId()));
		CrewInvite rejected = crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId() + 1));
		rejected.reject();
		crewInviteRepository.save(rejected);
		authUserRepository.save(fullUser(3L, "target2-provider", "other"));

		List<GetMyCrewInvitesUseCase.View> result = getMyCrewInvitesUseCase.handle(
			GetMyCrewInvitesUseCase.Query.of(target.getId())
		);

		assertThat(result).singleElement()
			.extracting(
				GetMyCrewInvitesUseCase.View::crewId,
				GetMyCrewInvitesUseCase.View::crewName,
				GetMyCrewInvitesUseCase.View::inviterNickname,
				GetMyCrewInvitesUseCase.View::status
			)
			.containsExactly(crew.getId(), "??? ??", "leader", "PENDING");
	}

	@Test
	void hidesInvitesForDeletedCrew() {
		Crew activeCrew = crewRepository.save(Crew.create("?? ??", "crew", CrewVisibility.PRIVATE, null));
		Crew deletedCrew = crewRepository.save(Crew.create("??? ??", "crew", CrewVisibility.PRIVATE, null));
		deletedCrew.delete();
		crewRepository.save(deletedCrew);

		AuthUser inviter = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "target");
		authUserRepository.save(inviter);
		authUserRepository.save(target);

		crewInviteRepository.save(CrewInvite.createPending(activeCrew.getId(), inviter.getId(), target.getId()));
		crewInviteRepository.save(CrewInvite.createPending(deletedCrew.getId(), inviter.getId(), target.getId()));

		List<GetMyCrewInvitesUseCase.View> result = getMyCrewInvitesUseCase.handle(
			GetMyCrewInvitesUseCase.Query.of(target.getId())
		);

		assertThat(result)
			.extracting(GetMyCrewInvitesUseCase.View::crewName)
			.containsExactly("?? ??");
	}

	@Test
	void acceptsPendingInviteAndCreatesCrewMembership() {
		Crew crew = crewRepository.save(Crew.create("??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser inviter = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "target");
		authUserRepository.save(inviter);
		authUserRepository.save(target);
		CrewInvite invite = crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId()));

		AcceptCrewInviteUseCase.Result result = acceptCrewInviteUseCase.handle(
			AcceptCrewInviteUseCase.Command.of(invite.getId(), target.getId())
		);

		assertThat(result.inviteId()).isEqualTo(invite.getId());
		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.status()).isEqualTo("APPROVED");
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), target.getId())).isTrue();
		assertThat(crewInviteRepository.findById(invite.getId())).hasValueSatisfying(savedInvite ->
			assertThat(savedInvite.getStatus()).isEqualTo(CrewInviteStatus.APPROVED)
		);
	}

	@Test
	void rejectsPendingInviteAndKeepsHistory() {
		Crew crew = crewRepository.save(Crew.create("??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser inviter = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "target");
		authUserRepository.save(inviter);
		authUserRepository.save(target);
		CrewInvite invite = crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId()));

		RejectCrewInviteUseCase.Result result = rejectCrewInviteUseCase.handle(
			RejectCrewInviteUseCase.Command.of(invite.getId(), target.getId())
		);

		assertThat(result.inviteId()).isEqualTo(invite.getId());
		assertThat(result.status()).isEqualTo("REJECTED");
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), target.getId())).isFalse();
		assertThat(crewInviteRepository.findById(invite.getId())).hasValueSatisfying(savedInvite ->
			assertThat(savedInvite.getStatus()).isEqualTo(CrewInviteStatus.REJECTED)
		);
	}

	@Test
	void rejectsInviteProcessingForTempUser() {
		Crew crew = crewRepository.save(Crew.create("??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser inviter = fullUser(1L, "leader-provider", "leader");
		AuthUser target = tempUser(2L, "target-provider");
		authUserRepository.save(inviter);
		authUserRepository.save(target);
		CrewInvite invite = crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId()));

		assertThatThrownBy(() -> acceptCrewInviteUseCase.handle(
			AcceptCrewInviteUseCase.Command.of(invite.getId(), target.getId())
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsAlreadyProcessedInvite() {
		Crew crew = crewRepository.save(Crew.create("??? ??", "crew", CrewVisibility.PRIVATE, null));
		AuthUser inviter = fullUser(1L, "leader-provider", "leader");
		AuthUser target = fullUser(2L, "target-provider", "target");
		authUserRepository.save(inviter);
		authUserRepository.save(target);
		CrewInvite invite = crewInviteRepository.save(CrewInvite.createPending(crew.getId(), inviter.getId(), target.getId()));
		invite.reject();
		crewInviteRepository.save(invite);

		assertThatThrownBy(() -> acceptCrewInviteUseCase.handle(
			AcceptCrewInviteUseCase.Command.of(invite.getId(), target.getId())
		)).isInstanceOf(com.bangpot.crew.application.exception.CrewInviteNotFoundException.class);
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
		public User save(User user) {
			throw new UnsupportedOperationException();
		}

		private User toDomain(AuthUser authUser) {
			return User.create(authUser.getId(), authUser.getNickname());
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
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
			return Optional.ofNullable(crewsById.get(crewId))
				.filter(crew -> crew.getStatus() == CrewStatus.ACTIVE);
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
		public Optional<Crew> findAnyById(Long crewId) {
			return Optional.ofNullable(crewsById.get(crewId));
		}

		@Override
		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getStatus() == CrewStatus.ACTIVE)
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
		public Optional<CrewInvite> findById(Long inviteId) {
			return Optional.ofNullable(invitesById.get(inviteId));
		}

		@Override
		public List<CrewInvite> findByTargetUserId(Long targetUserId) {
			return invitesById.values().stream()
				.filter(invite -> targetUserId.equals(invite.getTargetUserId()))
				.toList();
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
	}
}

