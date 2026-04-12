package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
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
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.CreateMeetingService;
import com.bangpot.meeting.application.service.GetMeetingDetailService;
import com.bangpot.meeting.application.service.GetMeetingsService;
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingResult;
import com.bangpot.meeting.domain.MeetingStatus;

class MeetingUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-12T10:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingRepository meetingRepository;
	private CreateMeetingUseCase createMeetingUseCase;
	private GetMeetingsUseCase getMeetingsUseCase;
	private GetMeetingDetailUseCase getMeetingDetailUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		createMeetingUseCase = new CreateMeetingService(authUserRepository, crewRepository, crewMemberRepository, meetingRepository);
		getMeetingsUseCase = new GetMeetingsService(authUserRepository, crewRepository, crewMemberRepository, meetingRepository);
		getMeetingDetailUseCase = new GetMeetingDetailService(authUserRepository, crewRepository, crewMemberRepository, meetingRepository);
	}

	@Test
	void createsMeetingWithDefaultStatusAndResultForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		CreateMeetingUseCase.Result result = createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(),
			member.getId(),
			"2026-04-20",
			"19:30",
			"강남점",
			"타임 어택",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"지각 없이 모일 분"
		));

		assertThat(result.meetingId()).isNotNull();
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.result()).isEqualTo("NOT_RECORDED");

		Meeting saved = meetingRepository.findById(result.meetingId()).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(MeetingStatus.RECRUITING);
		assertThat(saved.getResult()).isEqualTo(MeetingResult.NOT_RECORDED);
		assertThat(saved.getHostUserId()).isEqualTo(member.getId());
	}

	@Test
	void returnsMeetingsForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "딥 블루", "홍대점", "2026-04-21", "20:00", 6, null, null, null, null
		));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "타임 어택", "강남점", "2026-04-20", "19:30", 4, null, null, null, null
		));

		List<GetMeetingsUseCase.View> result = getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), member.getId()));

		assertThat(result).hasSize(2);
		assertThat(result.get(0).themeName()).isEqualTo("타임 어택");
		assertThat(result.get(0).status()).isEqualTo("RECRUITING");
		assertThat(result.get(1).themeName()).isEqualTo("딥 블루");
	}

	@Test
	void returnsMeetingDetailForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			member.getId(),
			"타임 어택",
			"강남점",
			"2026-04-20",
			"19:30",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"지각 없이 모일 분"
		));

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.hostUserId()).isEqualTo(member.getId());
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.result()).isEqualTo("NOT_RECORDED");
	}

	@Test
	void rejectsMeetingCreateForNonMember() {
		AuthUser outsider = fullUser(88L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(), outsider.getId(), "2026-04-20", "19:30", "강남점", "타임 어택", 4, null, null, null, null
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingAccessForTempUser() {
		AuthUser tempUser = tempUser(89L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), tempUser.getId()));

		assertThatThrownBy(() -> getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), tempUser.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingDetailForUnknownMeeting() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		assertThatThrownBy(() -> getMeetingDetailUseCase.handle(GetMeetingDetailUseCase.Query.of(crew.getId(), 999L, member.getId())))
			.isInstanceOf(MeetingNotFoundException.class);
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
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId)
				.map(member -> member.getRole() == CrewRole.LEADER)
				.orElse(false);
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
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
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
	}
}
