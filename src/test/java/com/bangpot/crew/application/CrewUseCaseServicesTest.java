package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.crew.application.exception.DuplicateCrewNameException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.CreateCrewService;
import com.bangpot.crew.application.usecase.CreateCrewUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;

class CrewUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-08T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private CreateCrewUseCase createCrewUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		createCrewUseCase = new CreateCrewService(authUserRepository, crewRepository, crewMemberRepository);
	}

	@Test
	void createsCrewAndAssignsCreatorAsLeader() {
		AuthUser creator = fullUser(1L, "creator");
		authUserRepository.save(creator);

		CreateCrewUseCase.Result result = createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(creator.getId(), "  방팟 야식 크루  ", "같이 먹고 같이 달리기", null, null)
		);

		assertThat(result.crewId()).isNotNull();
		assertThat(result.name()).isEqualTo("방팟 야식 크루");
		assertThat(result.myRole()).isEqualTo(CrewRole.LEADER);
		assertThat(crewRepository.findById(result.crewId())).get()
			.extracting(Crew::getName, Crew::getDescription, Crew::getVisibility)
			.containsExactly("방팟 야식 크루", "같이 먹고 같이 달리기", CrewVisibility.PUBLIC);
		assertThat(crewMemberRepository.findLeaderByCrewId(result.crewId())).get()
			.extracting(CrewMember::getUserId, CrewMember::getRole)
			.containsExactly(creator.getId(), CrewRole.LEADER);
	}

	@Test
	void rejectsCrewCreationForTempUser() {
		AuthUser tempUser = tempUser(2L, "temp-user");
		authUserRepository.save(tempUser);

		assertThatThrownBy(() -> createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(tempUser.getId(), "방팟 야식 크루", null, "PUBLIC", null)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsDuplicateCrewName() {
		AuthUser creator = fullUser(1L, "creator");
		authUserRepository.save(creator);
		crewRepository.save(Crew.create("방팟 야식 크루", null, CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(creator.getId(), "방팟 야식 크루", null, "PUBLIC", null)
		))
			.isInstanceOf(DuplicateCrewNameException.class);
	}

	@Test
	void rejectsUnknownAuthenticatedUser() {
		assertThatThrownBy(() -> createCrewUseCase.handle(
			CreateCrewUseCase.Command.of(99L, "방팟 야식 크루", null, "PUBLIC", null)
		))
			.isInstanceOf(AuthUserNotFoundException.class);
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
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {

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

		Optional<CrewMember> findLeaderByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.getRole() == CrewRole.LEADER)
				.findFirst();
		}
	}
}
