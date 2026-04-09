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

class CrewJoinUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-08T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private GetCrewJoinViewUseCase getCrewJoinViewUseCase;
	private RequestCrewJoinUseCase requestCrewJoinUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		getCrewJoinViewUseCase = new GetCrewJoinViewService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		requestCrewJoinUseCase = new RequestCrewJoinService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
	}

	@Test
	void returnsGuestStatusForUnauthenticatedUserOnPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), null)
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.GUEST);
	}

	@Test
	void returnsCompletionRequiredStatusForTempUser() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
		AuthUser tempUser = tempUser(10L, "temp-user");
		authUserRepository.save(tempUser);

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), tempUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.COMPLETION_REQUIRED);
	}

	@Test
	void returnsCanRequestStatusForFullNonMemberOnPublicCrew() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(11L, "full-user");
		authUserRepository.save(fullUser);

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.CAN_REQUEST);
	}

	@Test
	void returnsPendingStatusWhenJoinRequestAlreadyExists() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(12L, "full-user");
		authUserRepository.save(fullUser);
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), fullUser.getId(), "같이 달리고 싶어요"));

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PENDING);
	}

	@Test
	void returnsMemberStatusWhenUserAlreadyJoinedCrew() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
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
		Crew crew = crewRepository.save(Crew.create("방팟 비공개 크루", "비공개", CrewVisibility.PRIVATE, null));
		AuthUser fullUser = fullUser(14L, "full-user");
		authUserRepository.save(fullUser);

		GetCrewJoinViewUseCase.Result result = getCrewJoinViewUseCase.handle(
			GetCrewJoinViewUseCase.Query.of(crew.getId(), fullUser.getId())
		);

		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PRIVATE_RESTRICTED);
	}

	@Test
	void createsPendingJoinRequestForEligibleFullUser() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(15L, "full-user");
		authUserRepository.save(fullUser);

		RequestCrewJoinUseCase.Result result = requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), "  같이 달리고 싶어요  ")
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.myStatus()).isEqualTo(CrewJoinViewStatus.PENDING);
		assertThat(crewJoinRequestRepository.findByCrewIdAndUserId(crew.getId(), fullUser.getId())).get()
			.extracting(CrewJoinRequest::getMessage, CrewJoinRequest::getStatus)
			.containsExactly("같이 달리고 싶어요", CrewJoinRequestStatus.PENDING);
	}

	@Test
	void rejectsJoinRequestForPrivateCrew() {
		Crew crew = crewRepository.save(Crew.create("방팟 비공개 크루", "비공개", CrewVisibility.PRIVATE, null));
		AuthUser fullUser = fullUser(16L, "full-user");
		authUserRepository.save(fullUser);

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), null)
		))
			.isInstanceOf(CrewJoinRequestNotAllowedException.class);
	}

	@Test
	void rejectsJoinRequestWhenAlreadyJoined() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(17L, "full-user");
		authUserRepository.save(fullUser);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), fullUser.getId()));

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), null)
		))
			.isInstanceOf(CrewAlreadyJoinedException.class);
	}

	@Test
	void rejectsJoinRequestWhenAlreadyPending() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
		AuthUser fullUser = fullUser(18L, "full-user");
		authUserRepository.save(fullUser);
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), fullUser.getId(), null));

		assertThatThrownBy(() -> requestCrewJoinUseCase.handle(
			RequestCrewJoinUseCase.Command.of(crew.getId(), fullUser.getId(), null)
		))
			.isInstanceOf(CrewJoinRequestAlreadyPendingException.class);
	}

	@Test
	void rejectsJoinRequestForTempUser() {
		Crew crew = crewRepository.save(Crew.create("방팟 러닝 크루", "달리기 크루", CrewVisibility.PUBLIC, null));
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

		@Override
		public java.util.List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
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

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.anyMatch(member ->
					crewId.equals(member.getCrewId()) &&
					userId.equals(member.getUserId()) &&
					member.getRole() == com.bangpot.crew.domain.CrewRole.LEADER
				);
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {

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
		public java.util.List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
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
