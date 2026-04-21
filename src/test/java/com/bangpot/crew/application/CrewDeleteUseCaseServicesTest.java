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
import com.bangpot.crew.application.exception.CrewDeleteNameMismatchException;
import com.bangpot.crew.application.exception.CrewDeleteNotAllowedWithActiveMeetingsException;
import com.bangpot.crew.application.exception.CrewDeleteNotAllowedWithActiveMembersException;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.DeleteCrewService;
import com.bangpot.crew.application.service.GetCrewHubService;
import com.bangpot.crew.application.service.GetPublicCrewCardsService;
import com.bangpot.crew.application.usecase.DeleteCrewUseCase;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewDeleteUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-14T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingRepository meetingRepository;
	private DeleteCrewUseCase deleteCrewUseCase;
	private GetCrewHubUseCase getCrewHubUseCase;
	private GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		CompletedUserAccessService completedUserAccessService = new CompletedUserAccessService(userRepository);
		deleteCrewUseCase = new DeleteCrewService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		getCrewHubUseCase = new GetCrewHubService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			new InMemoryCrewJoinRequestRepository()
		);
		getPublicCrewCardsUseCase = new GetPublicCrewCardsService(crewRepository);
	}

	@Test
	void deletesCrewWhenLeaderIsOnlyActiveMemberAndNameMatches() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		DeleteCrewUseCase.Result result = deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), leader.getId(), "Crew Alpha")
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(crewRepository.findById(crew.getId())).isEmpty();
		assertThat(crewRepository.findAnyById(crew.getId())).get()
			.extracting(Crew::getStatus)
			.isEqualTo(CrewStatus.DELETED);
		assertThat(crewMemberRepository.findAnyByCrewIdAndUserId(crew.getId(), leader.getId())).get()
			.extracting(CrewMember::getStatus)
			.isEqualTo(CrewMemberStatus.LEFT);
		assertThat(getPublicCrewCardsUseCase.handle()).isEmpty();
		assertThatThrownBy(() -> getCrewHubUseCase.handle(GetCrewHubUseCase.Query.of(crew.getId(), leader.getId())))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void rejectsDeleteWhenCurrentUserIsNotLeader() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		assertThatThrownBy(() -> deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), member.getId(), "Crew Alpha")
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsDeleteWhenAnotherActiveMemberExists() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser member = fullUser(2L, "member-provider", "member");
		authUserRepository.save(leader);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		assertThatThrownBy(() -> deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), leader.getId(), "Crew Alpha")
		)).isInstanceOf(CrewDeleteNotAllowedWithActiveMembersException.class);
	}

	@Test
	void ignoresLeftAndRemovedMembersWhenCheckingDeleteCondition() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		AuthUser leftMember = fullUser(2L, "left-provider", "left");
		AuthUser removedMember = fullUser(3L, "removed-provider", "removed");
		authUserRepository.save(leader);
		authUserRepository.save(leftMember);
		authUserRepository.save(removedMember);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		CrewMember left = CrewMember.createMember(crew.getId(), leftMember.getId());
		left.leave();
		crewMemberRepository.save(left);
		CrewMember removed = CrewMember.createMember(crew.getId(), removedMember.getId());
		removed.remove();
		crewMemberRepository.save(removed);

		DeleteCrewUseCase.Result result = deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), leader.getId(), "Crew Alpha")
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
	}

	@Test
	void rejectsDeleteWhenUnfinishedMeetingExists() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		meetingRepository.save(Meeting.create(
			crew.getId(), leader.getId(), "Theme A", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), leader.getId(), "Crew Alpha")
		)).isInstanceOf(CrewDeleteNotAllowedWithActiveMeetingsException.class);
	}

	@Test
	void allowsDeleteWhenMeetingsAreCompletedOrCanceled() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		Meeting closedAndCompleted = meetingRepository.save(Meeting.create(
			crew.getId(), leader.getId(), "Theme A", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		closedAndCompleted.closeRecruitment();
		closedAndCompleted.complete();
		meetingRepository.save(closedAndCompleted);

		Meeting canceled = meetingRepository.save(Meeting.create(
			crew.getId(), leader.getId(), "Theme B", "Hongdae", "2026-04-21", "20:00", 4, null, null, null, null
		));
		canceled.cancel();
		meetingRepository.save(canceled);

		DeleteCrewUseCase.Result result = deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), leader.getId(), "Crew Alpha")
		);

		assertThat(result.crewId()).isEqualTo(crew.getId());
	}

	@Test
	void rejectsDeleteWhenCrewNameDoesNotMatch() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> deleteCrewUseCase.handle(
			DeleteCrewUseCase.Command.of(crew.getId(), leader.getId(), "Crew Beta")
		)).isInstanceOf(CrewDeleteNameMismatchException.class);
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
			return authUserRepository.findById(userId)
				.filter(authUser -> authUser.getStatus() == AuthUserStatus.FULL)
				.map(authUser -> User.create(authUser.getId(), authUser.getNickname()));
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
			throw new UnsupportedOperationException();
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
			return Optional.ofNullable(crewsById.get(crewId))
				.filter(Crew::isActive);
		}		@Override
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
				.filter(Crew::isActive)
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
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.anyMatch(member -> crewId.equals(member.getCrewId())
					&& userId.equals(member.getUserId())
					&& member.getStatus() == CrewMemberStatus.ACTIVE
					&& member.getRole() == CrewRole.LEADER);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()))
				.filter(CrewMember::isActive)
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
				.filter(member -> crewId.equals(member.getCrewId()))
				.filter(CrewMember::isActive)
				.toList();
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {

		private final Map<Long, Meeting> meetingsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public Meeting save(Meeting meeting) {
			if (meeting.getId() == null) {
				meeting.assignId(sequence++);
			}
			meetingsById.put(meeting.getId(), meeting);
			return meeting;
		}

		@Override
		public List<Meeting> findAllByCrewId(Long crewId) {
			return meetingsById.values().stream()
				.filter(meeting -> crewId.equals(meeting.getCrewId()))
				.toList();
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return findById(meetingId).filter(meeting -> crewId.equals(meeting.getCrewId()));
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return 0L;
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return 0L;
		}

		@Override
		public boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<MeetingStatus> statuses) {
			return meetingsById.values().stream()
				.anyMatch(meeting -> crewId.equals(meeting.getCrewId())
					&& hostUserId.equals(meeting.getHostUserId())
					&& statuses.contains(meeting.getStatus()));
		}

		@Override
		public boolean existsByCrewIdAndStatusIn(Long crewId, List<MeetingStatus> statuses) {
			return meetingsById.values().stream()
				.anyMatch(meeting -> crewId.equals(meeting.getCrewId()) && statuses.contains(meeting.getStatus()));
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {

		private final Map<Long, CrewJoinRequest> requestsById = new HashMap<>();

		@Override
		public CrewJoinRequest save(CrewJoinRequest crewJoinRequest) {
			requestsById.put(crewJoinRequest.getId(), crewJoinRequest);
			return crewJoinRequest;
		}

		@Override
		public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
			return false;
		}

		@Override
		public List<CrewJoinRequest> findByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
			return requestsById.values().stream()
				.filter(request -> crewId.equals(request.getCrewId()))
				.filter(request -> request.getStatus() == CrewJoinRequestStatus.PENDING)
				.toList();
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
			return Optional.empty();
		}
	}
}
