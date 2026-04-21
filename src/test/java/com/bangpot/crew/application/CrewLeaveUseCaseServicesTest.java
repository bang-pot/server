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
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewJoinRequestRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetCrewHubService;
import com.bangpot.crew.application.usecase.LeaveCrewUseCase;
import com.bangpot.crew.application.service.LeaveCrewService;
import com.bangpot.crew.application.usecase.GetCrewHubUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class CrewLeaveUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-13T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryCrewJoinRequestRepository crewJoinRequestRepository;
	private InMemoryMeetingRepository meetingRepository;
	private LeaveCrewUseCase leaveCrewUseCase;
	private GetCrewHubUseCase getCrewHubUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		crewJoinRequestRepository = new InMemoryCrewJoinRequestRepository();
		meetingRepository = new InMemoryMeetingRepository();
		CompletedUserAccessService completedUserAccessService = new CompletedUserAccessService(userRepository);
		leaveCrewUseCase = new LeaveCrewService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		getCrewHubUseCase = new GetCrewHubService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			crewJoinRequestRepository
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
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && member.isActive())
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
			return meetingsById.values().stream()
				.filter(meeting -> meetingId.equals(meeting.getId()) && crewId.equals(meeting.getCrewId()))
				.findFirst();
		}

		@Override
		public boolean existsByCrewIdAndHostUserIdAndStatusIn(Long crewId, Long hostUserId, List<com.bangpot.meeting.domain.MeetingStatus> statuses) {
			return meetingsById.values().stream()
				.anyMatch(meeting ->
					crewId.equals(meeting.getCrewId()) &&
					hostUserId.equals(meeting.getHostUserId()) &&
					statuses.contains(meeting.getStatus())
				);
		}
	}

	private static final class InMemoryCrewJoinRequestRepository implements CrewJoinRequestRepository {

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
		public List<CrewJoinRequest> findPendingByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<CrewJoinRequest> findByCrewId(Long crewId) {
			return List.of();
		}
	}
}
