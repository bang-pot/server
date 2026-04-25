package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.port.MeetingLogFeedReadRepository;
import com.bangpot.meeting.application.service.GetCrewMeetingLogFeedService;
import com.bangpot.meeting.application.usecase.GetCrewMeetingLogFeedUseCase;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetCrewMeetingLogFeedServiceTest {

	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingLogFeedReadRepository meetingLogFeedReadRepository;
	private GetCrewMeetingLogFeedUseCase getCrewMeetingLogFeedUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingLogFeedReadRepository = new InMemoryMeetingLogFeedReadRepository();
		getCrewMeetingLogFeedUseCase = new GetCrewMeetingLogFeedService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			meetingLogFeedReadRepository
		);
	}

	@Test
	void returnsMeetingLogFeedForActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), 7L));
		meetingLogFeedReadRepository.result = MeetingLogFeedReadRepository.SearchResult.of(
			List.of(
				MeetingLogFeedReadRepository.Item.of(
					11L,
					31L,
					"writer",
					"Friday Escape",
					"2026-04-12",
					Instant.parse("2026-04-15T01:00:00Z"),
					"Quite a fun log body for card rendering.",
					"https://cdn.example.com/a.jpg",
					2L
				)
			),
			MeetingLogFeedReadRepository.PageInfo.of(0, 20, false)
		);

		GetCrewMeetingLogFeedUseCase.Result result = getCrewMeetingLogFeedUseCase.handle(
			GetCrewMeetingLogFeedUseCase.Query.of(crew.getId(), 7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingDate()).isEqualTo("2026-04-12");
		assertThat(result.items().get(0).excerpt()).isEqualTo("Quite a fun log body for card rendering.");
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/a.jpg");
		assertThat(result.items().get(0).extraPhotoCount()).isEqualTo(1L);
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void throwsCrewNotFoundWhenCrewDoesNotExist() {
		userRepository.save(User.create(7L, "member"));

		assertThatThrownBy(() -> getCrewMeetingLogFeedUseCase.handle(
			GetCrewMeetingLogFeedUseCase.Query.of(999L, 7L, 0, 20)
		))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void throwsAccessDeniedWhenUserIsNotActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewMeetingLogFeedUseCase.handle(
			GetCrewMeetingLogFeedUseCase.Query.of(crew.getId(), 7L, 0, 20)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	private static final class InMemoryUserRepository implements UserRepository {
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}

		private final java.util.Map<Long, User> users = new java.util.HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(users.get(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return users.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return users.values().stream()
				.filter(user -> user.getNickname().contains(nickname))
				.toList();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
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

		private final java.util.Map<Long, Crew> crews = new java.util.HashMap<>();
		private long sequence = 1L;

		@Override
		public boolean existsByName(String name) {
			return crews.values().stream().anyMatch(crew -> crew.getName().equals(name));
		}

		@Override
		public Crew save(Crew crew) {
			if (crew.getId() == null) {
				crew.assignId(sequence++);
			}
			crews.put(crew.getId(), crew);
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return Optional.ofNullable(crews.get(crewId))
				.filter(crew -> crew.getStatus() == com.bangpot.crew.domain.CrewStatus.ACTIVE);
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
		public Optional<Crew> findAnyById(Long crewId) {
			return Optional.ofNullable(crews.get(crewId));
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
		}@Override
		public List<Crew> findPublicCrews() {
			return crews.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId);
		}

		private final java.util.Map<Long, CrewMember> members = new java.util.HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			members.put(crewMember.getId(), crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return members.values().stream().anyMatch(member ->
				member.getCrewId().equals(crewId)
					&& member.getUserId().equals(userId)
					&& member.getStatus() == com.bangpot.crew.domain.CrewMemberStatus.ACTIVE
			);
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return false;
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return members.values().stream()
				.filter(member -> member.getCrewId().equals(crewId)
					&& member.getUserId().equals(userId)
					&& member.getStatus() == com.bangpot.crew.domain.CrewMemberStatus.ACTIVE)
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
			return members.values().stream()
				.filter(member -> member.getCrewId().equals(crewId))
				.toList();
		}
	}

	private static final class InMemoryMeetingLogFeedReadRepository implements MeetingLogFeedReadRepository {
		private SearchResult result = SearchResult.of(List.of(), PageInfo.of(0, 20, false));

		@Override
		public SearchResult search(Long crewId, int page, int size) {
			return result;
		}
	}
}


