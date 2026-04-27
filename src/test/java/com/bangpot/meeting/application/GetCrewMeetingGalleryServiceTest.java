package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.service.GetCrewMeetingGalleryService;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryView;
import com.bangpot.meeting.domain.view.CrewScheduleView;
import com.bangpot.meeting.domain.view.MeetingDetailView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MeetingsView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetCrewMeetingGalleryServiceTest {

	private InMemoryUserRepository userRepository;
	private InMemoryMeetingQueryRepository meetingQueryRepository;
	private GetCrewMeetingGalleryUseCase getCrewMeetingGalleryUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		meetingQueryRepository = new InMemoryMeetingQueryRepository();
		getCrewMeetingGalleryUseCase = new GetCrewMeetingGalleryService(
			new CompletedUserAccessService(userRepository),
			meetingQueryRepository
		);
	}

	@Test
	void returnsMeetingGalleryForActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.of(MeetingsAccessView.of(1L, CrewRole.MEMBER));
		meetingQueryRepository.galleryView = CrewMeetingGalleryView.of(
			List.of(
				CrewMeetingGalleryView.Item.of(
					31L,
					"2026-04-12",
					"금요일 이스케이프",
					"https://cdn.example.com/a.jpg",
					2L
				)
			),
			CrewMeetingGalleryView.Page.of(0, 20, false)
		);

		CrewMeetingGalleryView result = getCrewMeetingGalleryUseCase.handle(
			GetCrewMeetingGalleryUseCase.Query.of(1L, 7L, 0, 20)
		);

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).meetingId()).isEqualTo(31L);
		assertThat(result.items().get(0).meetingDate()).isEqualTo("2026-04-12");
		assertThat(result.items().get(0).coverPhotoUrl()).isEqualTo("https://cdn.example.com/a.jpg");
		assertThat(result.items().get(0).extraPhotoCount()).isEqualTo(2L);
		assertThat(result.page().hasNext()).isFalse();
	}

	@Test
	void throwsCrewNotFoundWhenCrewDoesNotExist() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.empty();

		assertThatThrownBy(() -> getCrewMeetingGalleryUseCase.handle(
			GetCrewMeetingGalleryUseCase.Query.of(999L, 7L, 0, 20)
		))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void throwsAccessDeniedWhenUserIsNotActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.of(MeetingsAccessView.of(1L, null));

		assertThatThrownBy(() -> getCrewMeetingGalleryUseCase.handle(
			GetCrewMeetingGalleryUseCase.Query.of(1L, 7L, 0, 20)
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
				.filter(user -> user.getNickname().contains(nickname))
				.toList();
		}

		@Override
		public List<User> findAllCompletedUsers() {
			return users.values().stream().toList();
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}

		@Override
		public void withdrawById(Long userId, String anonymizedNickname, Instant withdrawnAt) {
		}
	}

	private static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private Optional<MeetingsAccessView> accessView = Optional.empty();
		private CrewMeetingGalleryView galleryView = CrewMeetingGalleryView.of(
			List.of(),
			CrewMeetingGalleryView.Page.of(0, 20, false)
		);

		@Override
		public Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(Long crewId, Long userId) {
			return accessView;
		}

		@Override
		public MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return MyCalendarView.of(List.of(), 0);
		}

		@Override
		public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return MyCreatedMeetingsView.of(List.of(), MyCreatedMeetingsView.Page.of(page, size, false));
		}

		@Override
		public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return MyJoinedMeetingsView.of(List.of(), MyJoinedMeetingsView.Page.of(page, size, false));
		}

		@Override
		public UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
			Long userId,
			int limit,
			String currentDate,
			String currentTime
		) {
			return UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public CrewScheduleView findCrewScheduleViewByCrewId(Long crewId, LocalDate from, LocalDate to) {
			return CrewScheduleView.of(List.of());
		}

		@Override
		public CrewMeetingGalleryView findCrewMeetingGalleryView(Long crewId, int page, int size) {
			return galleryView;
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(
			Long crewId,
			Long meetingId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public List<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(
			Long meetingId
		) {
			return List.of();
		}

		@Override
		public MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
			return MeetingsView.of(List.of(), MeetingsView.Page.of(page, size, false));
		}

		@Override
		public Optional<MeetingDetailView> findMeetingDetailView(Long crewId, Long meetingId, Long userId) {
			return Optional.empty();
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
		public Map<Long, Integer> countCompletedByUserIds(Collection<Long> userIds) {
			return Map.of();
		}
	}
}
