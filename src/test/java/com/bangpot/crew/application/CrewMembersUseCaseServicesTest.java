package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewMembersService;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.crew.domain.view.CrewMembersView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewMembersUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-11T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private CompletedUserAccessService completedUserAccessService;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewQueryRepository crewQueryRepository;
	private GetCrewMembersUseCase getCrewMembersUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewQueryRepository = new InMemoryCrewQueryRepository(crewRepository, crewMemberRepository, userRepository);
		getCrewMembersUseCase = new GetCrewMembersService(
			completedUserAccessService,
			crewQueryRepository
		);
	}

	@Test
	void returnsCrewMembersSortedByLeaderThenJoinedAtDescending() {
		AuthUser requester = fullAuthUser(1L, "requester-provider", "requester");
		authUserRepository.save(requester);
		userRepository.save(User.create(1L, "requester"));
		userRepository.save(User.create(2L, "leader-pot"));
		userRepository.save(User.create(3L, "new-member"));
		userRepository.save(User.create(4L, "old-member"));

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(10L, crew.getId(), 2L, CrewRole.LEADER, NOW.minusSeconds(300)));
		crewMemberRepository.save(crewMember(11L, crew.getId(), 3L, CrewRole.MEMBER, NOW.minusSeconds(60)));
		crewMemberRepository.save(crewMember(12L, crew.getId(), 4L, CrewRole.MEMBER, NOW.minusSeconds(120)));
		crewMemberRepository.save(crewMember(13L, crew.getId(), 1L, CrewRole.MEMBER, NOW.minusSeconds(180)));

		CrewMembersView result = getCrewMembersUseCase.handle(
			GetCrewMembersUseCase.Query.of(crew.getId(), requester.getId())
		);

		assertThat(result.items()).extracting(CrewMembersView.Item::userId)
			.containsExactly(2L, 3L, 4L, 1L);
		assertThat(result.items()).extracting(CrewMembersView.Item::role)
			.containsExactly(CrewRole.LEADER, CrewRole.MEMBER, CrewRole.MEMBER, CrewRole.MEMBER);
		assertThat(result.items().get(0).nickname()).isEqualTo("leader-pot");
		assertThat(result.items().get(0).profileImageUrl()).isNull();
		assertThat(result.items().get(0).bio()).isNull();
		assertThat(result.items().get(0).gender()).isNull();
		assertThat(result.items().get(0).escapeCount()).isZero();
		assertThat(result.items().get(1).joinedAt()).isEqualTo("2026-04-10T23:59:00Z");
	}

	@Test
	void returnsSingleLeaderWhenCrewHasOnlyOneMember() {
		AuthUser leader = fullAuthUser(2L, "leader-provider", "leader-pot");
		authUserRepository.save(leader);
		userRepository.save(User.create(2L, "leader-pot"));

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(10L, crew.getId(), leader.getId(), CrewRole.LEADER, NOW.minusSeconds(60)));

		CrewMembersView result = getCrewMembersUseCase.handle(
			GetCrewMembersUseCase.Query.of(crew.getId(), leader.getId())
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).userId()).isEqualTo(leader.getId());
		assertThat(result.items().get(0).role()).isEqualTo(CrewRole.LEADER);
	}

	@Test
	void rejectsCrewMembersForNonMember() {
		AuthUser outsider = fullAuthUser(99L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		userRepository.save(User.create(99L, "outsider"));
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewMembersUseCase.handle(GetCrewMembersUseCase.Query.of(crew.getId(), outsider.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewMembersForTempUser() {
		AuthUser tempUser = tempAuthUser(88L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(crewMember(10L, crew.getId(), tempUser.getId(), CrewRole.MEMBER, NOW.minusSeconds(60)));

		assertThatThrownBy(() -> getCrewMembersUseCase.handle(GetCrewMembersUseCase.Query.of(crew.getId(), tempUser.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewMembersForUnknownCrew() {
		AuthUser requester = fullAuthUser(1L, "requester-provider", "requester");
		authUserRepository.save(requester);

		assertThatThrownBy(() -> getCrewMembersUseCase.handle(GetCrewMembersUseCase.Query.of(999L, requester.getId())))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void rejectsCrewMembersForUnknownUser() {
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewMembersUseCase.handle(GetCrewMembersUseCase.Query.of(crew.getId(), 999L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	private AuthUser fullAuthUser(Long id, String providerId, String nickname) {
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

	private AuthUser tempAuthUser(Long id, String providerId) {
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

	private CrewMember crewMember(Long id, Long crewId, Long userId, CrewRole role, Instant joinedAt) {
		CrewMember member = role == CrewRole.LEADER
			? CrewMember.createLeader(crewId, userId)
			: CrewMember.createMember(crewId, userId);
		member.assignId(id);
		return memberRepositoryView(member, joinedAt);
	}

	private CrewMember memberRepositoryView(CrewMember member, Instant joinedAt) {
		return new CrewMemberSnapshot(member.getId(), member.getCrewId(), member.getUserId(), member.getRole(), joinedAt);
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


		private final Map<Long, User> usersById = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return usersById.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			String normalized = nickname == null ? null : nickname.trim().toLowerCase();
			return usersById.values().stream()
				.filter(user -> normalized == null || user.getNickname().toLowerCase().contains(normalized))
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


		private final List<CrewMember> members = new ArrayList<>();

		@Override
		public CrewMember save(CrewMember crewMember) {
			members.removeIf(existing -> existing.getId().equals(crewMember.getId()));
			members.add(crewMember);
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
			return members.stream()
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
			return members.stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.sorted(Comparator
					.comparing((CrewMember member) -> member.getRole() != CrewRole.LEADER)
					.thenComparing(CrewMember::getCreatedAt, Comparator.reverseOrder()))
				.toList();
		}
	}

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryUserRepository userRepository;

		private InMemoryCrewQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository,
			InMemoryUserRepository userRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
			this.userRepository = userRepository;
		}

		@Override
		public Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<CrewMembersView> findCrewMembersViewByCrewIdAndUserId(Long crewId, Long userId) {
			if (crewRepository.findById(crewId).isEmpty()) {
				return Optional.empty();
			}
			CrewRole myRole = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
				.map(CrewMember::getRole)
				.orElse(null);
			List<CrewMembersView.Item> items = myRole == null
				? List.of()
				: crewMemberRepository.findAllByCrewId(crewId).stream()
					.map(this::toItem)
					.filter(java.util.Objects::nonNull)
					.toList();
			return Optional.of(CrewMembersView.of(myRole, items));
		}

		private CrewMembersView.Item toItem(CrewMember member) {
			return userRepository.findById(member.getUserId())
				.map(user -> CrewMembersView.Item.of(
					user.getId(),
					user.getNickname(),
					null,
					null,
					null,
					0,
					member.getRole(),
					member.getCreatedAt()
				))
				.orElse(null);
		}

		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(
			Long userId,
			int page,
			int size
		) {
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
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findMeetingCreateCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew>
		findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class CrewMemberSnapshot extends CrewMember {

		private final Long id;
		private final Long crewId;
		private final Long userId;
		private final CrewRole role;
		private final Instant createdAt;

		private CrewMemberSnapshot(Long id, Long crewId, Long userId, CrewRole role, Instant createdAt) {
			this.id = id;
			this.crewId = crewId;
			this.userId = userId;
			this.role = role;
			this.createdAt = createdAt;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public Long getCrewId() {
			return crewId;
		}

		@Override
		public Long getUserId() {
			return userId;
		}

		@Override
		public CrewRole getRole() {
			return role;
		}

		@Override
		public Instant getCreatedAt() {
			return createdAt;
		}
	}
}


