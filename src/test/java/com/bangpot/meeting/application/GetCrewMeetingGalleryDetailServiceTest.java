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
import com.bangpot.meeting.application.exception.MeetingGalleryNotFoundException;
import com.bangpot.meeting.application.port.MeetingGalleryReadRepository;
import com.bangpot.meeting.application.service.GetCrewMeetingGalleryDetailService;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetCrewMeetingGalleryDetailServiceTest {

	private InMemoryUserRepository userRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingGalleryReadRepository meetingGalleryReadRepository;
	private GetCrewMeetingGalleryDetailUseCase getCrewMeetingGalleryDetailUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingGalleryReadRepository = new InMemoryMeetingGalleryReadRepository();
		getCrewMeetingGalleryDetailUseCase = new GetCrewMeetingGalleryDetailService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository,
			meetingGalleryReadRepository
		);
	}

	@Test
	void returnsMeetingGalleryDetailForActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), 7L));
		meetingGalleryReadRepository.detail = Optional.of(
			MeetingGalleryReadRepository.Detail.of(
				31L,
				"2026-04-12",
				"Friday Escape",
				List.of(
					MeetingGalleryReadRepository.DetailPhoto.of(501L, "https://cdn.example.com/a.jpg", 1),
					MeetingGalleryReadRepository.DetailPhoto.of(502L, "https://cdn.example.com/b.jpg", 2)
				),
				2
			)
		);

		GetCrewMeetingGalleryDetailUseCase.Result result = getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(crew.getId(), 31L, 7L)
		);

		assertThat(result.meetingId()).isEqualTo(31L);
		assertThat(result.meetingDate()).isEqualTo("2026-04-12");
		assertThat(result.meetingTitle()).isEqualTo("Friday Escape");
		assertThat(result.totalPhotoCount()).isEqualTo(2);
		assertThat(result.photos()).extracting(GetCrewMeetingGalleryDetailUseCase.Photo::photoId)
			.containsExactly(501L, 502L);
		assertThat(result.photos()).extracting(GetCrewMeetingGalleryDetailUseCase.Photo::order)
			.containsExactly(1, 2);
	}

	@Test
	void throwsMeetingGalleryNotFoundWhenMeetingIsNotGalleryTarget() {
		userRepository.save(User.create(7L, "member"));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), 7L));

		assertThatThrownBy(() -> getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(crew.getId(), 999L, 7L)
		))
			.isInstanceOf(MeetingGalleryNotFoundException.class);
	}

	@Test
	void throwsCrewNotFoundWhenCrewDoesNotExist() {
		userRepository.save(User.create(7L, "member"));

		assertThatThrownBy(() -> getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(999L, 31L, 7L)
		))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void throwsAccessDeniedWhenUserIsNotActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		Crew crew = crewRepository.save(Crew.create("Alpha Crew", "desc", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(crew.getId(), 31L, 7L)
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
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return members.values().stream()
				.filter(member -> member.getCrewId().equals(crewId))
				.toList();
		}
	}

	private static final class InMemoryMeetingGalleryReadRepository implements MeetingGalleryReadRepository {
		private SearchResult result = SearchResult.of(List.of(), PageInfo.of(0, 20, false));
		private Optional<Detail> detail = Optional.empty();

		@Override
		public SearchResult search(Long crewId, int page, int size) {
			return result;
		}

		@Override
		public Optional<Detail> findDetail(Long crewId, Long meetingId) {
			return detail;
		}
	}
}


