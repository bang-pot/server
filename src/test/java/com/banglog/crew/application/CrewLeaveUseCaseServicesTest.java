package com.banglog.crew.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewJoinRequestRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.application.service.GetCrewHubService;
import com.banglog.crew.application.usecase.LeaveCrewUseCase;
import com.banglog.crew.application.service.LeaveCrewService;
import com.banglog.crew.application.usecase.GetCrewHubUseCase;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewJoinRequest;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewMemberStatus;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.port.MeetingParticipantRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.service.CleanupMeetingsForInactiveCrewMemberService;
import com.banglog.meeting.application.usecase.CleanupMeetingsForInactiveCrewMemberUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingParticipant;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.application.service.CompletedUserAccessService;
import com.banglog.user.domain.User;

class CrewLeaveUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-13T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private InMemoryMeetingRepository meetingRepository;
	private InMemoryMeetingParticipantRepository meetingParticipantRepository;
	private LeaveCrewUseCase leaveCrewUseCase;
	private GetCrewHubUseCase getCrewHubUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository(authUserRepository);
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		meetingRepository = new InMemoryMeetingRepository();
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository(meetingRepository);
		CompletedUserAccessService completedUserAccessService = new CompletedUserAccessService(userRepository);
		CleanupMeetingsForInactiveCrewMemberUseCase cleanupMeetingsForInactiveCrewMemberUseCase =
			new CleanupMeetingsForInactiveCrewMemberService(
				meetingRepository,
				meetingParticipantRepository,
				Clock.fixed(NOW, ZoneId.of("UTC"))
			);
		leaveCrewUseCase = new LeaveCrewService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			cleanupMeetingsForInactiveCrewMemberUseCase
		);
		getCrewHubUseCase = new GetCrewHubService(
			completedUserAccessService,
			new InMemoryCrewQueryRepository(
				crewRepository,
				crewMemberRepository,
				crewJoinRequestRepository
			)
		);
	}

	@Test
	void leavesCrewForGeneralMember() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		LeaveCrewUseCase.Result result = leaveCrewUseCase.handle(LeaveCrewUseCase.Command.of(crew.getId(), member.getId()));

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), member.getId())).isFalse();
		assertThat(crewMemberRepository.findAnyByCrewIdAndUserId(crew.getId(), member.getId())).get()
			.extracting(CrewMember::getStatus)
			.isEqualTo(CrewMemberStatus.LEFT);
		assertThatThrownBy(() -> getCrewHubUseCase.handle(GetCrewHubUseCase.Query.of(crew.getId(), member.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsLeaveForLeader() {
		AuthUser leader = fullUser(1L, "leader-provider", "leader");
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));

		assertThatThrownBy(() -> leaveCrewUseCase.handle(LeaveCrewUseCase.Command.of(crew.getId(), leader.getId())));
	}

	@Test
	void rejectsLeaveWhenUserHasUnfinishedHostedMeeting() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		meetingRepository.save(Meeting.create(
			crew.getId(),
			member.getId(),
			"Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));

		assertThatThrownBy(() -> leaveCrewUseCase.handle(LeaveCrewUseCase.Command.of(crew.getId(), member.getId())));
	}

	@Test
	void allowsLeaveWhenHostedMeetingIsCompleted() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			member.getId(),
			"Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));
		meeting.closeRecruitment();
		meeting.complete();

		LeaveCrewUseCase.Result result = leaveCrewUseCase.handle(LeaveCrewUseCase.Command.of(crew.getId(), member.getId()));

		assertThat(result.crewId()).isEqualTo(crew.getId());
		assertThat(crewMemberRepository.existsByCrewIdAndUserId(crew.getId(), member.getId())).isFalse();
		assertThat(crewMemberRepository.findAnyByCrewIdAndUserId(crew.getId(), member.getId())).get()
			.extracting(CrewMember::getStatus)
			.isEqualTo(CrewMemberStatus.LEFT);
	}

	@Test
	void rejectsLeaveForNonMember() {
		AuthUser outsider = fullUser(1L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> leaveCrewUseCase.handle(LeaveCrewUseCase.Command.of(crew.getId(), outsider.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void leavesJoinedUnfinishedMeetingParticipationsWhenLeavingCrew() {
		AuthUser member = fullUser(1L, "member-provider", "member");
		AuthUser host = fullUser(2L, "host-provider", "host");
		authUserRepository.save(member);
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));

		leaveCrewUseCase.handle(LeaveCrewUseCase.Command.of(crew.getId(), member.getId()));

		assertThat(meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), member.getId())).get()
			.extracting(MeetingParticipant::getStatus)
			.isEqualTo(MeetingParticipationStatus.LEFT);
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
		public java.util.List<com.banglog.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			throw new UnsupportedOperationException();
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
			return List.of();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.banglog.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
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
			return findByCrewIdAndUserId(crewId, userId).isPresent();
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
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()) && member.isActive())
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
			return findAllByCrewId(crewId).stream()
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.isActive())
				.toList();
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {
		@Override
		public com.banglog.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.banglog.meeting.domain.view.MyCalendarView.of(java.util.List.of(), 0);
		}

		@Override
		public com.banglog.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return com.banglog.meeting.domain.view.MyJoinedMeetingsView.of(
				java.util.List.of(),
				com.banglog.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.banglog.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return com.banglog.meeting.domain.view.MyCreatedMeetingsView.of(
				java.util.List.of(),
				com.banglog.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
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
		public List<Meeting> findRecruitmentCloseTargets(java.time.LocalDateTime now, int limit) {
			return List.of();
		}

		@Override
		public List<Meeting> findCompletionTargets(java.time.LocalDateTime completionCutoff, int limit) {
			return List.of();
		}

		@Override
		public int cancelUnfinishedByCrewIdAndHostUserId(
			Long crewId,
			Long hostUserId,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return meetingsById.values().stream()
				.filter(meeting -> meetingId.equals(meeting.getId()) && crewId.equals(meeting.getCrewId()))
				.findFirst();
		}

		@Override
		public Optional<Meeting> findByIdAndCrewIdForUpdate(Long meetingId, Long crewId) {
			return findByIdAndCrewId(meetingId, crewId);
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
		public boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId) {
			return meetingsById.values().stream()
				.anyMatch(meeting -> crewId.equals(meeting.getCrewId())
					&& hostUserId.equals(meeting.getHostUserId())
					&& List.of(
						com.banglog.meeting.domain.MeetingStatus.RECRUITING,
						com.banglog.meeting.domain.MeetingStatus.RECRUITMENT_CLOSED
					).contains(meeting.getStatus()));
		}

		@Override
		public boolean existsUnfinishedByCrewId(Long crewId) {
			return meetingsById.values().stream()
				.anyMatch(meeting -> crewId.equals(meeting.getCrewId())
					&& List.of(
						com.banglog.meeting.domain.MeetingStatus.RECRUITING,
						com.banglog.meeting.domain.MeetingStatus.RECRUITMENT_CLOSED
					).contains(meeting.getStatus()));
		}
	}

	private static final class InMemoryMeetingParticipantRepository implements MeetingParticipantRepository {

		private final InMemoryMeetingRepository meetingRepository;
		private final Map<Long, MeetingParticipant> participantsById = new HashMap<>();
		private long sequence = 1L;

		private InMemoryMeetingParticipantRepository(InMemoryMeetingRepository meetingRepository) {
			this.meetingRepository = meetingRepository;
		}

		@Override
		public MeetingParticipant save(MeetingParticipant participant) {
			if (participant.getId() == null) {
				participant.assignId(sequence++);
			}
			participantsById.put(participant.getId(), participant);
			return participant;
		}

		@Override
		public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
			return participantsById.values().stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()) && userId.equals(participant.getUserId()))
				.findFirst();
		}

		@Override
		public int leaveInactiveCrewMemberParticipations(
			Long crewId,
			Long userId,
			java.time.Instant updatedAt
		) {
			List<MeetingParticipant> targetParticipants = participantsById.values().stream()
				.filter(participant -> userId.equals(participant.getUserId()))
				.filter(participant -> participant.getStatus().representsJoined())
				.filter(participant -> meetingRepository.findById(participant.getMeetingId())
					.filter(meeting -> crewId.equals(meeting.getCrewId()))
					.filter(meeting -> !userId.equals(meeting.getHostUserId()))
					.filter(meeting -> List.of(
						com.banglog.meeting.domain.MeetingStatus.RECRUITING,
						com.banglog.meeting.domain.MeetingStatus.RECRUITMENT_CLOSED
					).contains(meeting.getStatus()))
					.isPresent())
				.toList();
			targetParticipants.forEach(MeetingParticipant::leave);
			return targetParticipants.size();
		}

		@Override
		public long countByMeetingId(Long meetingId) {
			return participantsById.values().stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()))
				.filter(participant -> participant.getStatus().representsJoined())
				.count();
		}

		@Override
		public void delete(MeetingParticipant participant) {
			participantsById.remove(participant.getId());
		}
	}

	private static final class InMemoryCrewQueryRepository implements com.banglog.crew.application.port.CrewQueryRepository {

		@Override
		public com.banglog.crew.domain.view.ExploreCrewCardsView findExploreCrewCardsView(
			String keyword,
			com.banglog.crew.domain.ExploreCrewSort sort,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.banglog.crew.domain.view.CrewInviteCandidateAccessView>
			findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.banglog.crew.domain.view.CrewInviteCandidatesView findCrewInviteCandidatesView(
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
		public java.util.Optional<com.banglog.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.banglog.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
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
					return com.banglog.crew.domain.view.CrewHubView.of(
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
		public java.util.Optional<com.banglog.crew.domain.view.CrewMemberAccessView>
			findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.banglog.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}


		@Override
		public java.util.Optional<com.banglog.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}		@Override
		public com.banglog.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.banglog.crew.domain.view.PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
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
		public com.banglog.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.List<com.banglog.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			throw new UnsupportedOperationException();
		}
	}
	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {
		@Override
		public java.util.Optional<com.banglog.crew.domain.CrewJoinRequest> findPendingByIdAndUserId(Long requestId, Long userId) {
			return java.util.Optional.empty();
		}

		@Override
		public java.util.Optional<com.banglog.crew.domain.CrewJoinRequest> findPendingByIdAndUserIdForUpdate(
			Long requestId,
			Long userId
		) {
			return findPendingByIdAndUserId(requestId, userId);
		}


		@Override
		public CrewJoinRequest save(CrewJoinRequest crewJoinRequest) {
			return crewJoinRequest;
		}

		@Override
		public boolean existsPendingByCrewIdAndUserId(Long crewId, Long userId) {
			return false;
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewId(Long requestId, Long crewId) {
			return Optional.empty();
		}

		@Override
		public Optional<CrewJoinRequest> findPendingByIdAndCrewIdForUpdate(Long requestId, Long crewId) {
			return findPendingByIdAndCrewId(requestId, crewId);
		}

		@Override
		public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<CrewJoinRequest> findByCrewId(Long crewId) {
			return List.of();
		}
	}
}


