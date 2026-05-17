package com.banglog.crew.application;

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

import com.banglog.auth.application.port.AuthUserRepository;
import com.banglog.auth.domain.AuthProvider;
import com.banglog.auth.domain.AuthUser;
import com.banglog.auth.domain.AuthUserStatus;
import com.banglog.auth.domain.RequiredTermsAgreement;
import com.banglog.crew.application.exception.DuplicateCrewNameException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.service.CreateCrewService;
import com.banglog.crew.application.usecase.CreateCrewUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.image.application.usecase.AttachImageUploadUseCase;
import com.banglog.image.domain.ImageUploadCategory;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.application.service.CompletedUserAccessService;
import com.banglog.user.domain.User;

class CrewUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-08T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private CreateCrewUseCase createCrewUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		createCrewUseCase = new CreateCrewService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			new FakeAttachImageUploadUseCase()
		);
	}

	@Test
	void createsCrewAndAssignsCreatorAsLeader() {
		AuthUser creator = fullUser(1L, "creator");
		authUserRepository.save(creator);

		CreateCrewUseCase.Result result = createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(creator.getId(), "  ?? ?? ??  ", "?? ?? ?? ??", null, null)
		);

		assertThat(result.crewId()).isNotNull();
		assertThat(result.name()).isEqualTo("?? ?? ??");
		assertThat(result.myRole()).isEqualTo(CrewRole.LEADER);
		assertThat(crewRepository.findById(result.crewId())).get()
			.extracting(Crew::getName, Crew::getDescription, Crew::getVisibility)
			.containsExactly("?? ?? ??", "?? ?? ?? ??", CrewVisibility.PUBLIC);
		assertThat(crewMemberRepository.findLeaderByCrewId(result.crewId())).get()
			.extracting(CrewMember::getUserId, CrewMember::getRole)
			.containsExactly(creator.getId(), CrewRole.LEADER);
	}

	@Test
	void createsCrewWithAttachedCoverImage() {
		AuthUser creator = fullUser(1L, "creator");
		authUserRepository.save(creator);

		CreateCrewUseCase.Result result = createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(creator.getId(), "image crew", null, "PUBLIC", 200L)
		);

		assertThat(crewRepository.findById(result.crewId())).get()
			.extracting(Crew::getImageUrl)
			.isEqualTo("https://cdn.example.com/crew-cover-images/200.jpg");
	}

	@Test
	void rejectsCrewCreationForTempUser() {
		AuthUser tempUser = tempUser(2L, "temp-user");
		authUserRepository.save(tempUser);

		assertThatThrownBy(() -> createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(tempUser.getId(), "?? ?? ??", null, "PUBLIC", null)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsDuplicateCrewName() {
		AuthUser creator = fullUser(1L, "creator");
		authUserRepository.save(creator);
		crewRepository.save(Crew.create("?? ?? ??", null, CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(creator.getId(), "  ?? ?? ??  ", "?? ?? ?? ??", null, null)
		))
			.isInstanceOf(DuplicateCrewNameException.class);
	}

	@Test
	void rejectsUnknownAuthenticatedUser() {
		assertThatThrownBy(() -> createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(99L, "?? ?? ??", null, "PUBLIC", null)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	private AuthUser fullUser(Long id, String providerId) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			"banglog",
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

	private static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public java.util.Optional<com.banglog.crew.domain.Crew> findAnyById(Long crewId) {
			return findById(crewId);
		}

		@Override
		public com.banglog.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.banglog.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.banglog.crew.domain.view.MyCrewsView.Page.of(page, size, false)
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
		public java.util.List<com.banglog.user.domain.User> findAllCompletedUsers() {
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
		public java.util.List<com.banglog.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.banglog.crew.domain.CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
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

		Optional<CrewMember> findLeaderByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.getRole() == CrewRole.LEADER)
				.findFirst();
		}
	}

	private static final class FakeAttachImageUploadUseCase implements AttachImageUploadUseCase {

		@Override
		public Result handle(Command command) {
			if (command.category() != ImageUploadCategory.CREW_COVER_IMAGE) {
				throw new IllegalArgumentException("unexpected category");
			}
			return Result.of(command.uploadIds().stream()
				.map(uploadId -> "https://cdn.example.com/crew-cover-images/" + uploadId + ".jpg")
				.toList());
		}
	}
}

