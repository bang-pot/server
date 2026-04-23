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
import com.bangpot.crew.application.port.CrewPolicyRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewPoliciesService;
import com.bangpot.crew.application.usecase.GetCrewPoliciesUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewPolicy;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewPoliciesUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-12T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private CompletedUserAccessService completedUserAccessService;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewPolicyRepository crewPolicyRepository;
	private GetCrewPoliciesUseCase getCrewPoliciesUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewPolicyRepository = new InMemoryCrewPolicyRepository();
		getCrewPoliciesUseCase = new GetCrewPoliciesService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			crewPolicyRepository
		);
	}

	@Test
	void returnsCrewPoliciesForJoinedMember() {
		AuthUser requester = fullAuthUser(1L, "requester-provider", "requester");
		authUserRepository.save(requester);

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "private crew", CrewVisibility.PRIVATE, null));
		crewMemberRepository.save(crewMember(10L, crew.getId(), requester.getId(), CrewRole.MEMBER, NOW.minusSeconds(120)));
		crewPolicyRepository.save(policy(100L, crew.getId(), "?? ??", "?? ??? ?????."));
		crewPolicyRepository.save(policy(101L, crew.getId(), "?? ??", "??? ?????.\n?? ? ?? ?????."));

		List<GetCrewPoliciesUseCase.View> result = getCrewPoliciesUseCase.handle(
			GetCrewPoliciesUseCase.Query.of(crew.getId(), requester.getId())
		);

		assertThat(result).extracting(GetCrewPoliciesUseCase.View::policyId)
			.containsExactly(100L, 101L);
		assertThat(result.get(0).title()).isEqualTo("?? ??");
		assertThat(result.get(1).content()).isEqualTo("??? ?????.\n?? ? ?? ?????.");
	}

	@Test
	void returnsEmptyListWhenCrewHasNoPolicies() {
		AuthUser requester = fullAuthUser(1L, "requester-provider", "requester");
		authUserRepository.save(requester);

		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "private crew", CrewVisibility.PRIVATE, null));
		crewMemberRepository.save(crewMember(10L, crew.getId(), requester.getId(), CrewRole.MEMBER, NOW.minusSeconds(120)));

		List<GetCrewPoliciesUseCase.View> result = getCrewPoliciesUseCase.handle(
			GetCrewPoliciesUseCase.Query.of(crew.getId(), requester.getId())
		);

		assertThat(result).isEmpty();
	}

	@Test
	void rejectsCrewPoliciesForNonMember() {
		AuthUser outsider = fullAuthUser(99L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "private crew", CrewVisibility.PRIVATE, null));

		assertThatThrownBy(() -> getCrewPoliciesUseCase.handle(GetCrewPoliciesUseCase.Query.of(crew.getId(), outsider.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewPoliciesForTempUser() {
		AuthUser tempUser = tempAuthUser(88L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "private crew", CrewVisibility.PRIVATE, null));
		crewMemberRepository.save(crewMember(10L, crew.getId(), tempUser.getId(), CrewRole.MEMBER, NOW.minusSeconds(60)));

		assertThatThrownBy(() -> getCrewPoliciesUseCase.handle(GetCrewPoliciesUseCase.Query.of(crew.getId(), tempUser.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsCrewPoliciesForUnknownCrew() {
		AuthUser requester = fullAuthUser(1L, "requester-provider", "requester");
		authUserRepository.save(requester);

		assertThatThrownBy(() -> getCrewPoliciesUseCase.handle(GetCrewPoliciesUseCase.Query.of(999L, requester.getId())))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void rejectsCrewPoliciesForUnknownUser() {
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "private crew", CrewVisibility.PRIVATE, null));

		assertThatThrownBy(() -> getCrewPoliciesUseCase.handle(GetCrewPoliciesUseCase.Query.of(crew.getId(), 999L)))
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
		return new CrewMemberSnapshot(member.getId(), member.getCrewId(), member.getUserId(), member.getRole(), joinedAt);
	}

	private CrewPolicy policy(Long id, Long crewId, String title, String content) {
		CrewPolicy policy = CrewPolicy.create(crewId, title, content);
		policy.assignId(id);
		return policy;
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
				.sorted(Comparator.comparing(Crew::getId))
				.toList();
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

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId);
		}


		private final List<CrewMember> crewMembers = new ArrayList<>();

		@Override
		public CrewMember save(CrewMember crewMember) {
			crewMembers.removeIf(existing -> existing.getId().equals(crewMember.getId()));
			crewMembers.add(crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return crewMembers.stream().anyMatch(member -> member.getCrewId().equals(crewId) && member.getUserId().equals(userId));
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return crewMembers.stream().anyMatch(member ->
				member.getCrewId().equals(crewId)
					&& member.getUserId().equals(userId)
					&& member.getRole() == CrewRole.LEADER
			);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return crewMembers.stream()
				.filter(member -> member.getCrewId().equals(crewId) && member.getUserId().equals(userId))
				.findFirst();
		}

		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return crewMembers.stream()
				.filter(member -> member.getCrewId().equals(crewId))
				.toList();
		}
	}

	private static final class InMemoryCrewPolicyRepository implements CrewPolicyRepository {

		private final List<CrewPolicy> policies = new ArrayList<>();

		@Override
		public CrewPolicy save(CrewPolicy crewPolicy) {
			policies.removeIf(existing -> existing.getId().equals(crewPolicy.getId()));
			policies.add(crewPolicy);
			return crewPolicy;
		}

		@Override
		public List<CrewPolicy> findAllByCrewId(Long crewId) {
			return policies.stream()
				.filter(policy -> policy.getCrewId().equals(crewId))
				.sorted(Comparator.comparing(CrewPolicy::getId))
				.toList();
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

