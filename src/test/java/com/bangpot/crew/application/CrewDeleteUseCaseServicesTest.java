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
			new InMemoryCrewQueryRepository(
				crewRepository,
				crewMemberRepository,
				new InMemoryCrewJoinRequestRepository()
			)
		);
		getPublicCrewCardsUseCase = new GetPublicCrewCardsService(new InMemoryCrewQueryRepository(
			crewRepository,
			crewMemberRepository,
			new InMemoryCrewJoinRequestRepository()
		));
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
		assertThat(crewRepository.findByIdForUpdateCallCount).isEqualTo(1);
		assertThat(crewRepository.findById(crew.getId())).isEmpty();
		assertThat(crewRepository.findAnyById(crew.getId())).get()
			.extracting(Crew::getStatus)
			.isEqualTo(CrewStatus.DELETED);
		assertThat(crewMemberRepository.findAnyByCrewIdAndUserId(crew.getId(), leader.getId())).get()
			.extracting(CrewMember::getStatus)
			.isEqualTo(CrewMemberStatus.LEFT);
		assertThat(getPublicCrewCardsUseCase.handle(GetPublicCrewCardsUseCase.Query.of(0, 20)).items()).isEmpty();
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
		assertThat(crewMemberRepository.findAllByCrewIdCallCount).isZero();
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
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			throw new UnsupportedOperationException();
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
		private int findByIdForUpdateCallCount;

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
		}

		@Override
		public Optional<Crew> findByIdForUpdate(Long crewId) {
			findByIdForUpdateCallCount++;
			return Optional.ofNullable(crewsById.get(crewId))
				.filter(Crew::isActive);
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
		public Optional<Crew> findAnyById(Long crewId) {
			return Optional.ofNullable(crewsById.get(crewId));
		}

		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(Crew::isActive)
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}


		private final Map<Long, CrewMember> membersById = new HashMap<>();
		private long sequence = 1L;
		private int findAllByCrewIdCallCount;

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
		public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			findAllByCrewIdCallCount++;
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.filter(CrewMember::isActive)
				.toList();
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {
		@Override
		public com.bangpot.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MyCalendarView.of(java.util.List.of(), 0);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return com.bangpot.meeting.domain.view.MyJoinedMeetingsView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return com.bangpot.meeting.domain.view.MyCreatedMeetingsView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}


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

	private static final class InMemoryCrewQueryRepository implements com.bangpot.crew.application.port.CrewQueryRepository {

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewInviteCandidateAccessView>
			findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.CrewInviteCandidatesView findCrewInviteCandidatesView(
			Long crewId,
			Long leaderUserId,
			String nickname,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;
		private final InMemoryCrewJoinRequestRepository crewJoinRequestRepository;

		private InMemoryCrewQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository,
			InMemoryCrewJoinRequestRepository crewJoinRequestRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
			this.crewJoinRequestRepository = crewJoinRequestRepository;
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			return crewRepository.findById(crewId)
				.map(crew -> {
					CrewRole myRole = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
						.map(CrewMember::getRole)
						.orElse(null);
					Integer pendingCount = myRole == CrewRole.LEADER
						? crewJoinRequestRepository.findPendingByCrewId(crewId).size()
						: null;
					return com.bangpot.crew.domain.view.CrewHubView.of(
						crew.getId(),
						crew.getName(),
						crew.getDescription(),
						crew.getVisibility(),
						crew.getImageUrl(),
						myRole,
						false,
						pendingCount
					);
				});
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMemberAccessView>
			findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}


		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
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
		public com.bangpot.crew.domain.view.PublicCrewCardsView findPublicCrewCardsView(int page, int size) {
			List<com.bangpot.crew.domain.view.PublicCrewCardsView.Item> items = crewRepository.findPublicCrews().stream()
				.map(crew -> com.bangpot.crew.domain.view.PublicCrewCardsView.Item.of(
					crew.getId(),
					crew.getName(),
					crew.getDescription(),
					crew.getImageUrl()
				))
				.toList();
			int fromIndex = Math.min(page * size, items.size());
			int toIndex = Math.min(fromIndex + size, items.size());
			return com.bangpot.crew.domain.view.PublicCrewCardsView.of(
				items.subList(fromIndex, toIndex),
				com.bangpot.crew.domain.view.PublicCrewCardsView.Page.of(page, size, toIndex < items.size())
			);
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
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}
	}
	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {
		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
			return java.util.Optional.empty();
		}


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


