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
import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.ApproveCrewJoinRequestService;
import com.bangpot.crew.application.service.GetCrewJoinRequestsService;
import com.bangpot.crew.application.service.GetPendingCrewJoinRequestsService;
import com.bangpot.crew.application.service.RejectCrewJoinRequestService;
import com.bangpot.crew.application.usecase.ApproveCrewJoinRequestUseCase;
import com.bangpot.crew.application.usecase.GetCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.GetPendingCrewJoinRequestsUseCase;
import com.bangpot.crew.application.usecase.RejectCrewJoinRequestUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.domain.User;

class CrewJoinRequestReviewUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-09T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private GetCrewJoinRequestsUseCase getCrewJoinRequestsUseCase;
	private GetPendingCrewJoinRequestsUseCase getPendingCrewJoinRequestsUseCase;
	private ApproveCrewJoinRequestUseCase approveCrewJoinRequestUseCase;
	private RejectCrewJoinRequestUseCase rejectCrewJoinRequestUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		getCrewJoinRequestsUseCase = new GetCrewJoinRequestsService(
			userRepository,
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		getPendingCrewJoinRequestsUseCase = new GetPendingCrewJoinRequestsService(
			userRepository,
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		approveCrewJoinRequestUseCase = new ApproveCrewJoinRequestService(
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
		rejectCrewJoinRequestUseCase = new RejectCrewJoinRequestService(
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
		);
	}

	@Test
	void returnsAllJoinRequestsForLeaderManagementView() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser pendingUser = fullUser(2L, "pending-provider", "runner");
		AuthUser rejectedUser = fullUser(3L, "rejected-provider", "guest");
		authUserRepository.save(leader);
		authUserRepository.save(pendingUser);
		authUserRepository.save(rejectedUser);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), pendingUser.getId(), "같이 뛰고 싶어요"));
		CrewJoinRequest rejectedRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), rejectedUser.getId(), "저녁 러닝 가능합니다")
		);
		rejectedRequest.reject();
		crewJoinRequestRepository.save(rejectedRequest);

		List<GetCrewJoinRequestsUseCase.View> result = getCrewJoinRequestsUseCase.handle(
			GetCrewJoinRequestsUseCase.Query.of(crew.getId(), leader.getId())
		);

		assertThat(result).hasSize(2);
		assertThat(result.get(0))
			.extracting(
				GetCrewJoinRequestsUseCase.View::requestId,
				GetCrewJoinRequestsUseCase.View::userId,
				GetCrewJoinRequestsUseCase.View::nickname,
				GetCrewJoinRequestsUseCase.View::message,
				GetCrewJoinRequestsUseCase.View::status
			)
			.containsExactly(1L, 2L, "runner", "같이 뛰고 싶어요", "PENDING");
		assertThat(result.get(1))
			.extracting(
				GetCrewJoinRequestsUseCase.View::requestId,
				GetCrewJoinRequestsUseCase.View::userId,
				GetCrewJoinRequestsUseCase.View::nickname,
				GetCrewJoinRequestsUseCase.View::message,
				GetCrewJoinRequestsUseCase.View::status
			)
			.containsExactly(2L, 3L, "guest", "저녁 러닝 가능합니다", "REJECTED");
	}

	@Test
	void returnsPendingJoinRequestsForLeader() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser requester = fullUser(2L, "requester-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewJoinRequestRepository.save(CrewJoinRequest.createPending(crew.getId(), requester.getId(), "가입 요청"));

		List<GetPendingCrewJoinRequestsUseCase.View> result = getPendingCrewJoinRequestsUseCase.handle(
			GetPendingCrewJoinRequestsUseCase.Query.of(crew.getId(), leader.getId())
		);

		assertThat(result).singleElement()
			.extracting(
				GetPendingCrewJoinRequestsUseCase.View::requestId,
				GetPendingCrewJoinRequestsUseCase.View::userId,
				GetPendingCrewJoinRequestsUseCase.View::nickname
			)
			.containsExactly(1L, 2L, "runner");
	}

	@Test
	void rejectsPendingJoinRequestListingForNonLeader() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser member = fullUser(3L, "member-provider", "member");
		authUserRepository.save(member);

		assertThatThrownBy(() -> getPendingCrewJoinRequestsUseCase.handle(
			GetPendingCrewJoinRequestsUseCase.Query.of(crew.getId(), member.getId())
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void approvesPendingJoinRequestAndCreatesMember() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(10L, "leader-provider", "leader");
		AuthUser requester = fullUser(11L, "requester-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), "가입 요청")
		);

		ApproveCrewJoinRequestUseCase.Result result = approveCrewJoinRequestUseCase.handle(
			ApproveCrewJoinRequestUseCase.Command.of(crew.getId(), joinRequest.getId(), leader.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.requestId()).isEqualTo(joinRequest.getId());
		assertThat(result.userId()).isEqualTo(requester.getId());
		assertThat(result.role()).isEqualTo(CrewRole.MEMBER);
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), requester.getId())).isTrue();
		assertThat(crewJoinRequestRepository.findPendingByIdAndCrewId(joinRequest.getId(), crew.getId())).isEmpty();
		assertThat(crewJoinRequestRepository.findById(joinRequest.getId())).get()
			.extracting(CrewJoinRequest::getStatus)
			.isEqualTo(CrewJoinRequestStatus.APPROVED);
	}

	@Test
	void rejectsPendingJoinRequestForLeader() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(20L, "leader-provider", "leader");
		AuthUser requester = fullUser(21L, "requester-provider", "runner");
		authUserRepository.save(leader);
		authUserRepository.save(requester);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), "가입 요청")
		);

		RejectCrewJoinRequestUseCase.Result result = rejectCrewJoinRequestUseCase.handle(
			RejectCrewJoinRequestUseCase.Command.of(crew.getId(), joinRequest.getId(), leader.getId())
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(result.requestId()).isEqualTo(joinRequest.getId());
		assertThat(crewJoinRequestRepository.findPendingByIdAndCrewId(joinRequest.getId(), crew.getId())).isEmpty();
		assertThat(crewJoinRequestRepository.findById(joinRequest.getId())).get()
			.extracting(CrewJoinRequest::getStatus)
			.isEqualTo(CrewJoinRequestStatus.REJECTED);
	}

	@Test
	void rejectsApproveForNonLeader() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser nonLeader = fullUser(30L, "member-provider", "member");
		AuthUser requester = fullUser(31L, "requester-provider", "runner");
		authUserRepository.save(nonLeader);
		authUserRepository.save(requester);
		CrewJoinRequest joinRequest = crewJoinRequestRepository.save(
			CrewJoinRequest.createPending(crew.getId(), requester.getId(), "가입 요청")
		);

		assertThatThrownBy(() -> approveCrewJoinRequestUseCase.handle(
			ApproveCrewJoinRequestUseCase.Command.of(crew.getId(), joinRequest.getId(), nonLeader.getId())
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsUnknownPendingJoinRequest() {
		Crew crew = crewRepository.save(Crew.create("방팟 야식 크루", "crew", CrewVisibility.PUBLIC, null));
		AuthUser leader = fullUser(40L, "leader-provider", "leader");
		authUserRepository.save(leader);
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> rejectCrewJoinRequestUseCase.handle(
			RejectCrewJoinRequestUseCase.Command.of(crew.getId(), 999L, leader.getId())
		))
			.isInstanceOf(CrewJoinRequestNotFoundException.class);
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

	private static final class InMemoryUserRepository implements UserRepository {

		private final InMemoryAuthUserRepository authUserRepository;

		private InMemoryUserRepository(InMemoryAuthUserRepository authUserRepository) {
			this.authUserRepository = authUserRepository;
		}

		@Override
		public Optional<User> findById(Long userId) {
			return authUserRepository.findById(userId).map(this::toDomain);
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return authUserRepository.existsByNickname(nickname);
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			String normalizedKeyword = nickname == null ? null : nickname.trim().toLowerCase();
			if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
				normalizedKeyword = null;
			}
			final String keyword = normalizedKeyword;
			return authUserRepository.usersById.values().stream()
				.filter(user -> user.getStatus() == AuthUserStatus.FULL)
				.filter(user -> keyword == null || user.getNickname().toLowerCase().contains(keyword))
				.sorted((left, right) -> Long.compare(left.getId(), right.getId()))
				.map(this::toDomain)
				.toList();
		}

		@Override
		public User save(User user) {
			AuthUser authUser = authUserRepository.findById(user.getId()).orElseThrow();
			authUser.updateNickname(user.getNickname());
			authUserRepository.save(authUser);
			return toDomain(authUser);
		}

		private User toDomain(AuthUser authUser) {
			return User.rehydrate(authUser.getId(), authUser.getNickname(), authUser.getStatus() == AuthUserStatus.FULL);
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
					member.getRole() == CrewRole.LEADER
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

		Optional<CrewJoinRequest> findById(Long requestId) {
			return Optional.ofNullable(requestsById.get(requestId));
		}
	}
}
