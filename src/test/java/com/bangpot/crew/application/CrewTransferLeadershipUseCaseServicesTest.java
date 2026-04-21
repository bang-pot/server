package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.lang.reflect.Field;
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
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewHubService;
import com.bangpot.crew.application.service.GetCrewMembersService;
import com.bangpot.crew.application.service.TransferCrewLeadershipService;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.application.usecase.TransferCrewLeadershipUseCase;
import com.bangpot.crew.application.exception.CrewTransferLeadershipTargetNotAllowedException;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewTransferLeadershipUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-13T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private TransferCrewLeadershipUseCase transferCrewLeadershipUseCase;
	private GetCrewHubUseCase getCrewHubUseCase;
	private GetCrewMembersUseCase getCrewMembersUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		CompletedUserAccessService completedUserAccessService = new CompletedUserAccessService(userRepository);
		transferCrewLeadershipUseCase = new TransferCrewLeadershipService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository
		);
		getCrewHubUseCase = new GetCrewHubService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			new InMemoryCrewJoinRequestRepository()
		);
		getCrewMembersUseCase = new GetCrewMembersService(
			completedUserAccessService,
			userRepository,
			crewRepository,
			crewMemberRepository
		);
	}

	@Test
	void transfersLeadershipToCurrentMember() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser member = fullUser(2L, "member-provider", "member");
		authUserRepository.save(leader);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		TransferCrewLeadershipUseCase.Result result = transferCrewLeadershipUseCase.handle(
			TransferCrewLeadershipUseCase.Command.of(crew.getId(), leader.getId(), member.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.leaderUserId()).isEqualTo(member.getId());
		assertThat(crewMemberRepository.findByCrewIdAndUserId(crew.getId(), leader.getId())).get()
			.extracting(CrewMember::getRole)
			.isEqualTo(CrewRole.MEMBER);
		assertThat(crewMemberRepository.findByCrewIdAndUserId(crew.getId(), member.getId())).get()
			.extracting(CrewMember::getRole)
			.isEqualTo(CrewRole.LEADER);

		assertThat(getCrewHubUseCase.handle(GetCrewHubUseCase.Query.of(crew.getId(), leader.getId())).myRole())
			.isEqualTo(CrewRole.MEMBER);
		assertThat(getCrewHubUseCase.handle(GetCrewHubUseCase.Query.of(crew.getId(), member.getId())).myRole())
			.isEqualTo(CrewRole.LEADER);
		assertThat(getCrewMembersUseCase.handle(GetCrewMembersUseCase.Query.of(crew.getId(), member.getId())))
			.extracting(GetCrewMembersUseCase.View::role)
			.containsExactly(CrewRole.LEADER, CrewRole.MEMBER);
	}

	@Test
	void rejectsTransferWhenCurrentUserIsNotLeader() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		AuthUser anotherMember = fullUser(2L, "another-member-provider", "another-member");
		authUserRepository.save(member);
		authUserRepository.save(anotherMember);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), anotherMember.getId()));

		assertThatThrownBy(() -> transferCrewLeadershipUseCase.handle(
			TransferCrewLeadershipUseCase.Command.of(crew.getId(), member.getId(), anotherMember.getId())
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsTransferWhenTargetIsNotCurrentMember() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser outsider = fullUser(2L, "outsider-provider", "outsider");
		authUserRepository.save(leader);
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> transferCrewLeadershipUseCase.handle(
			TransferCrewLeadershipUseCase.Command.of(crew.getId(), leader.getId(), outsider.getId())
		))
			.isInstanceOf(CrewTransferLeadershipTargetNotAllowedException.class);
	}

	@Test
	void rejectsTransferWhenTargetIsCurrentLeader() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> transferCrewLeadershipUseCase.handle(
			TransferCrewLeadershipUseCase.Command.of(crew.getId(), leader.getId(), leader.getId())
		))
			.isInstanceOf(CrewTransferLeadershipTargetNotAllowedException.class);
	}

	private AuthUser fullUser(Long id, String providerId, String nickname) {
		userRepository.save(User.rehydrate(id, nickname));
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {

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

		@Override
		public AuthUser save(AuthUser user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {
		private final Map<Long, User> usersById = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return false;
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return List.of();
		}

		@Override
		public User save(User user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {

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
		public List<Crew> findPublicCrews() {
			return List.of();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {

		private final Map<Long, CrewMember> membersById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			if (crewMember.getCreatedAt() == null) {
				setCreatedAt(crewMember, NOW);
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

		private void setCreatedAt(CrewMember crewMember, Instant createdAt) {
			try {
				Field createdAtField = CrewMember.class.getDeclaredField("createdAt");
				createdAtField.setAccessible(true);
				createdAtField.set(crewMember, createdAt);
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("?뚯뒪?몄슜 CrewMember ?앹꽦?쇱떆瑜??ㅼ젙?????놁뒿?덈떎.", exception);
			}
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements com.bangpot.crew.application.port.CrewJoinRequestRepository {

		@Override
		public com.bangpot.crew.domain.CrewJoinRequest save(com.bangpot.crew.domain.CrewJoinRequest crewJoinRequest) {
			return crewJoinRequest;
		}

		@Override
		public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
			return false;
		}

		@Override
		public Optional<com.bangpot.crew.domain.CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
			return Optional.empty();
		}

		@Override
		public List<com.bangpot.crew.domain.CrewJoinRequest> findPendingByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<com.bangpot.crew.domain.CrewJoinRequest> findByCrewId(Long crewId) {
			return List.of();
		}
	}
}
