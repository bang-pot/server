package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import com.bangpot.meeting.application.port.MeetingHistoryReadRepository;
import com.bangpot.meeting.application.service.GetCrewMeetingHistoryService;
import com.bangpot.meeting.application.usecase.GetCrewMeetingHistoryUseCase;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetCrewMeetingHistoryServiceTest {

	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingHistoryReadRepository meetingHistoryReadRepository;
	private GetCrewMeetingHistoryUseCase getCrewMeetingHistoryUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingHistoryReadRepository = new InMemoryMeetingHistoryReadRepository();
		getCrewMeetingHistoryUseCase = new GetCrewMeetingHistoryService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			meetingHistoryReadRepository
		);
	}

	@Test
	void returnsCompletedMeetingHistoryWithMyLogStatus() {
		userRepository.save(User.rehydrate(7L, "member", true));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), 7L));
		meetingHistoryReadRepository.result = MeetingHistoryReadRepository.SearchResult.of(
			List.of(
				MeetingHistoryReadRepository.Item.of(
					31L,
					"금요일 이스케이프",
					"Deep Blue",
					"Hongdae",
					"2026-04-12",
					"SUCCESS",
					101L
				),
				MeetingHistoryReadRepository.Item.of(
					30L,
					"토요일 이스케이프",
					"Lost Harbor",
					"Busan",
					"2026-04-11",
					"FAILURE",
					null
				)
			),
			MeetingHistoryReadRepository.PageInfo.of(0, 20, false)
		);

		GetCrewMeetingHistoryUseCase.Result result = getCrewMeetingHistoryUseCase.handle(
			GetCrewMeetingHistoryUseCase.Query.of(crew.getId(), 7L, 0, 20)
		);

		assertThat(result.items()).hasSize(2);
		assertThat(result.items().get(0).myLogStatus()).isEqualTo("HAS_LOG");
		assertThat(result.items().get(0).logId()).isEqualTo(101L);
		assertThat(result.items().get(1).myLogStatus()).isEqualTo("NO_LOG");
		assertThat(result.items().get(1).logId()).isNull();
		assertThat(result.pageInfo().hasNext()).isFalse();
	}

	@Test
	void throwsCrewNotFoundWhenCrewDoesNotExist() {
		userRepository.save(User.rehydrate(7L, "member", true));

		assertThatThrownBy(() -> getCrewMeetingHistoryUseCase.handle(
			GetCrewMeetingHistoryUseCase.Query.of(999L, 7L, 0, 20)
		))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void throwsAccessDeniedWhenUserIsNotActiveCrewMember() {
		userRepository.save(User.rehydrate(7L, "member", true));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewMeetingHistoryUseCase.handle(
			GetCrewMeetingHistoryUseCase.Query.of(crew.getId(), 7L, 0, 20)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	private static final class InMemoryUserRepository implements UserRepository {
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
				.filter(user -> !user.requiresCompletion())
				.filter(user -> user.getNickname().contains(nickname))
				.toList();
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
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
		public Optional<Crew> findAnyById(Long crewId) {
			return Optional.ofNullable(crews.get(crewId));
		}

		@Override
		public List<Crew> findPublicCrews() {
			return crews.values().stream().filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC).toList();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
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
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return members.values().stream().filter(member -> member.getCrewId().equals(crewId)).toList();
		}
	}

	private static final class InMemoryMeetingHistoryReadRepository implements MeetingHistoryReadRepository {
		private SearchResult result = SearchResult.of(List.of(), PageInfo.of(0, 20, false));

		@Override
		public SearchResult search(Long crewId, Long userId, int page, int size) {
			return result;
		}
	}
}
